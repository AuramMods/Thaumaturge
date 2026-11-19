package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.electronwill.nightconfig.core.CommentedConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WaterConversionRuntimeTest {
    @Test
    void nativeTickReportsCustomTargetAndHonorsVetoAndTimerBeforeConverting() throws Exception {
        ServerLevel level = level();
        var husk = new CustomHusk(level);
        husk.setPos(10.5, 62.0, 8.5);
        husk.setCustomName(Component.literal("Port conversion"));
        husk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
        readyToConvert(husk);

        List<EntityType<?>> targets = new ArrayList<>();
        List<Entity> converted = new ArrayList<>();
        Consumer<LivingConversionEvent.Pre> postpone = event -> {
            if (event.getEntity() != husk) {
                return;
            }
            targets.add(event.getOutcome());
            if (targets.size() == 1) {
                event.setConversionTimer(2);
                event.setCanceled(true);
            }
        };
        Consumer<LivingConversionEvent.Post> record = event -> {
            if (event.getEntity() == husk) {
                converted.add(event.getOutcome());
            }
        };
        var bus = MinecraftForge.EVENT_BUS;
        Consumer<LivingEvent.LivingTickEvent> skipMovement = skipMovementFor(husk);
        ForgeConfigSpec forgeSpec = forgeServerSpec();
        boolean loadConfig = !forgeSpec.isLoaded();
        if (loadConfig) {
            var config = CommentedConfig.inMemory();
            forgeSpec.correct(config);
            forgeSpec.setConfig(config);
        }
        bus.addListener(postpone);
        bus.addListener(record);
        bus.addListener(skipMovement);
        try {
            husk.tick();
            assertFalse(husk.isRemoved());
            assertEquals(List.of(EntityType.ZOMBIE), targets);
            assertTrue(converted.isEmpty());
            assertEquals(2, husk.saveWithoutId(new CompoundTag()).getInt("DrownedConversionTime"));
            verify(level, never()).addFreshEntity(any());

            husk.tick();
            husk.tick();
            assertEquals(1, targets.size());
            husk.tick();
        } finally {
            bus.unregister(skipMovement);
            bus.unregister(record);
            bus.unregister(postpone);
            if (loadConfig) {
                forgeSpec.setConfig(null);
            }
        }

        assertEquals(List.of(EntityType.ZOMBIE, EntityType.ZOMBIE), targets);
        var result = ArgumentCaptor.forClass(Entity.class);
        verify(level).addFreshEntity(result.capture());
        Zombie replacement = assertInstanceOf(Zombie.class, result.getValue());
        assertEquals(List.of(replacement), converted);
        assertSame(EntityType.ZOMBIE, replacement.getType());
        assertTrue(husk.isRemoved());
        assertEquals(husk.position(), replacement.position());
        assertEquals(husk.getCustomName(), replacement.getCustomName());
        assertTrue(replacement.getMainHandItem().is(Items.IRON_SHOVEL));
    }

    @Test
    void regularZombiesKeepTheirNativeDrownedEvent() {
        ServerLevel level = level();
        var zombie = new Zombie(EntityType.ZOMBIE, level);
        readyToConvert(zombie);
        List<EntityType<?>> targets = new ArrayList<>();
        Consumer<LivingConversionEvent.Pre> veto = event -> {
            if (event.getEntity() == zombie) {
                targets.add(event.getOutcome());
                event.setCanceled(true);
            }
        };
        Consumer<LivingEvent.LivingTickEvent> skipMovement = skipMovementFor(zombie);
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(veto);
        bus.addListener(skipMovement);
        try {
            zombie.tick();
        } finally {
            bus.unregister(skipMovement);
            bus.unregister(veto);
        }
        assertEquals(List.of(EntityType.DROWNED), targets);
        assertFalse(zombie.isRemoved());
        verify(level, never()).addFreshEntity(any());
    }

    private static void readyToConvert(Zombie zombie) {
        var initial = new CompoundTag();
        initial.putInt("DrownedConversionTime", 0);
        zombie.readAdditionalSaveData(initial);
    }

    private static Consumer<LivingEvent.LivingTickEvent> skipMovementFor(Zombie zombie) {
        return event -> {
            // Zombie's conversion step runs before the ordinary living-entity tick.
            if (event.getEntity() == zombie) {
                event.setCanceled(true);
            }
        };
    }

    private static ServerLevel level() {
        var level = mock(ServerLevel.class);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);
        when(level.getCurrentDifficultyAt(any())).thenReturn(new DifficultyInstance(Difficulty.NORMAL, 0, 0, 0.0F));
        when(level.addFreshEntity(any())).thenReturn(true);
        return level;
    }

    private static ForgeConfigSpec forgeServerSpec() throws ReflectiveOperationException {
        var field = ForgeConfig.class.getDeclaredField("serverSpec");
        field.setAccessible(true);
        return (ForgeConfigSpec) field.get(null);
    }

    private static final class CustomHusk extends Husk implements WaterConversionTarget {
        private CustomHusk(ServerLevel level) {
            super(EntityType.HUSK, level);
        }

        @Override
        public EntityType<? extends Zombie> waterConversionTarget() {
            return EntityType.ZOMBIE;
        }
    }
}
