package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.event.combat.DamagePhaseEvent;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DamagePhaseRuntimeTest {
    private static final DamageSource SOURCE = new DamageSource(Holder.direct(new DamageType("port_damage", 0.0F)));

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void phasesBracketAbsorptionAndTheFinalForgeAdjustedHealthDamage(boolean player) throws Exception {
        var entity = victim(player);
        entity.getAttribute(TTAttributes.MAX_ABSORPTION.get()).setBaseValue(2);
        entity.setAbsorptionAmount(2);
        var observed = new ArrayList<Observation>();
        Consumer<DamagePhaseEvent> observe = event -> {
            assertSame(entity, event.getEntity());
            assertSame(SOURCE, event.getSource());
            observed.add(new Observation(event instanceof DamagePhaseEvent.BeforeAbsorption,
                    event.getNewDamage(), entity.getAbsorptionAmount(), entity.getHealth()));
        };
        Consumer<LivingDamageEvent> change = event -> {
            if (event.getEntity() == entity) event.setAmount(event.getAmount() * 0.5F);
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        MinecraftForge.EVENT_BUS.addListener(change);
        try {
            damage(entity, 12.0F);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(change);
            MinecraftForge.EVENT_BUS.unregister(observe);
        }
        assertEquals(List.of(new Observation(true, 5.0F, 2.0F, 20.0F),
                new Observation(false, 1.5F, 0.0F, 18.5F)), observed);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void fullyAbsorbedHitsEmitOneZeroDamageResultAndCancelledHitsEmitNone(boolean player) throws Exception {
        var entity = victim(player);
        entity.getAttribute(TTAttributes.MAX_ABSORPTION.get()).setBaseValue(7);
        entity.setAbsorptionAmount(7);
        var observed = new ArrayList<Observation>();
        Consumer<DamagePhaseEvent> observe = event -> observed.add(new Observation(
                event instanceof DamagePhaseEvent.BeforeAbsorption, event.getNewDamage(),
                entity.getAbsorptionAmount(), entity.getHealth()));
        Consumer<LivingHurtEvent> cancel = event -> {
            if (event.getEntity() == entity) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            damage(entity, 12.0F);
            var expected = List.of(new Observation(true, 5.0F, 7.0F, 20.0F),
                    new Observation(false, 0.0F, 2.0F, 20.0F));
            assertEquals(expected, observed);
            MinecraftForge.EVENT_BUS.addListener(cancel);
            try {
                damage(entity, 12.0F);
                assertEquals(expected, observed);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancel);
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observe);
        }
    }

    private static void damage(LivingEntity entity, float amount) throws Exception {
        var method = LivingEntity.class.getDeclaredMethod("actuallyHurt", DamageSource.class, float.class);
        method.setAccessible(true);
        method.invoke(entity, SOURCE, amount);
    }

    private static LivingEntity victim(boolean player) {
        var level = mock(Level.class);
        if (player) {
            return new Player(level, BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "PortDamage")) {
                @Override public boolean isSpectator() { return false; }
                @Override public boolean isCreative() { return false; }
                @Override protected float getDamageAfterArmorAbsorb(DamageSource source, float damage) { return damage - 2; }
                @Override protected float getDamageAfterMagicAbsorb(DamageSource source, float damage) { return damage * 0.5F; }
            };
        }
        return new Zombie(EntityType.ZOMBIE, level) {
            @Override protected float getDamageAfterArmorAbsorb(DamageSource source, float damage) { return damage - 2; }
            @Override protected float getDamageAfterMagicAbsorb(DamageSource source, float damage) { return damage * 0.5F; }
        };
    }

    private record Observation(boolean before, float damage, float absorption, float health) {}
}
