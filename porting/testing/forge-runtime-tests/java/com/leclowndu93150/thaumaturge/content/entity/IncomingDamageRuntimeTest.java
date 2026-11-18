package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.event.combat.IncomingDamageEvent;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.junit.jupiter.api.Test;

class IncomingDamageRuntimeTest {
    private static final DamageSource SOURCE = new DamageSource(Holder.direct(new DamageType("port_incoming", 0.0F)));
    private static final DamageSource SCALED = new DamageSource(
            Holder.direct(new DamageType("port_scaled", DamageScaling.ALWAYS, 0.0F)));

    @Test
    void rejectedHitsSkipTheEventAndCancellationLeavesSleepAndCooldownUntouched() {
        var entity = new RecordingZombie();
        entity.sleeping = true;
        var observed = new ArrayList<Float>();
        Consumer<IncomingDamageEvent> cancel = event -> {
            if (event.getEntity() == entity) {
                assertTrue(entity.isSleeping());
                observed.add(event.getAmount());
                event.setCanceled(true);
            }
        };
        Consumer<LivingAttackEvent> reject = event -> {
            if (event.getEntity() == entity && event.getAmount() == 8.0F) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(cancel);
        MinecraftForge.EVENT_BUS.addListener(reject);
        try {
            entity.setInvulnerable(true);
            assertFalse(entity.hurt(SOURCE, 4.0F));
            entity.setInvulnerable(false);
            assertFalse(entity.hurt(SOURCE, 8.0F));
            assertFalse(entity.hurt(SOURCE, 12.0F));
            assertEquals(List.of(12.0F), observed);
            assertTrue(entity.isSleeping());
            assertEquals(0, entity.invulnerableTime);
            assertTrue(entity.applied.isEmpty());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(reject);
            MinecraftForge.EVENT_BUS.unregister(cancel);
        }
    }

    @Test
    void editedAmountFlowsThroughNativeShieldHandling() {
        var entity = new RecordingZombie();
        entity.blocking = true;
        var observed = new ArrayList<Float>();
        Consumer<IncomingDamageEvent> change = event -> {
            if (event.getEntity() == entity) {
                assertSame(SOURCE, event.getSource());
                assertEquals(16.0F, event.getOriginalAmount());
                event.setAmount(8.0F);
            }
        };
        Consumer<IncomingDamageEvent> observe = event -> {
            if (event.getEntity() == entity) {
                assertEquals(16.0F, event.getOriginalAmount());
                observed.add(event.getAmount());
            }
        };
        Consumer<ShieldBlockEvent> block = event -> {
            if (event.getEntity() == entity) {
                observed.add(event.getOriginalBlockedDamage());
                event.setBlockedDamage(3.0F);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, change);
        MinecraftForge.EVENT_BUS.addListener(observe);
        MinecraftForge.EVENT_BUS.addListener(block);
        try {
            assertTrue(entity.hurt(SOURCE, 16.0F));
            assertEquals(List.of(8.0F, 8.0F), observed);
            assertEquals(List.of(5.0F), entity.applied);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(block);
            MinecraftForge.EVENT_BUS.unregister(observe);
            MinecraftForge.EVENT_BUS.unregister(change);
        }
    }

    @Test
    void nestedDamageOwnsItsAmountAndCooldownUsesTheEditedDamage() {
        var entity = new RecordingZombie();
        var observed = new ArrayList<Float>();
        Consumer<IncomingDamageEvent> change = event -> {
            if (event.getEntity() == entity) {
                observed.add(event.getOriginalAmount());
                if (event.getOriginalAmount() == 16.0F) {
                    assertTrue(entity.hurt(SOURCE, 4.0F));
                }
                event.setAmount(event.getAmount() * 0.5F);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(change);
        try {
            assertTrue(entity.hurt(SOURCE, 16.0F));
            assertFalse(entity.hurt(SOURCE, 12.0F));
            assertEquals(List.of(16.0F, 4.0F, 12.0F), observed);
            assertEquals(List.of(2.0F, 6.0F), entity.applied);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(change);
        }
    }

    @Test
    void playersPostOnceAfterDifficultyScalingAndApplyTheEditedAmount() {
        var level = mock(Level.class);
        when(level.getDifficulty()).thenReturn(Difficulty.HARD);
        var applied = new ArrayList<Float>();
        var entity = new Player(level, BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "PortIncoming")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
            @Override protected void actuallyHurt(DamageSource source, float damage) { applied.add(damage); }
        };
        var observed = new ArrayList<Float>();
        Consumer<IncomingDamageEvent> change = event -> {
            if (event.getEntity() == entity) {
                assertSame(SCALED, event.getSource());
                observed.add(event.getOriginalAmount());
                event.setAmount(3.0F);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(change);
        try {
            assertTrue(entity.hurt(SCALED, 6.0F));
            assertEquals(List.of(9.0F), observed);
            assertEquals(List.of(3.0F), applied);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(change);
        }
    }

    private static final class RecordingZombie extends Zombie {
        private final List<Float> applied = new ArrayList<>();
        private boolean sleeping;
        private boolean blocking;

        RecordingZombie() {
            super(EntityType.ZOMBIE, mock(ServerLevel.class));
        }

        @Override public boolean isSleeping() { return sleeping; }
        @Override public void stopSleeping() { sleeping = false; }
        @Override public boolean isDamageSourceBlocked(DamageSource source) { return blocking; }
        @Override protected void actuallyHurt(DamageSource source, float damage) { applied.add(damage); }
    }
}
