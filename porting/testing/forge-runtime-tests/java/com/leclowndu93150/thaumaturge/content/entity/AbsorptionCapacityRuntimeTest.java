package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;

class AbsorptionCapacityRuntimeTest {
    @Test
    void nativeMobAndPlayerAmountsUseTheRegisteredCap() {
        var attribute = TTAttributes.MAX_ABSORPTION.get();
        assertEquals(ResourceLocation.withDefaultNamespace("generic.max_absorption"), ForgeRegistries.ATTRIBUTES.getKey(attribute));
        assertEquals(0.0, attribute.getDefaultValue());
        assertEquals(2048.0, attribute.sanitizeValue(4096.0));
        assertTrue(attribute.isClientSyncable());

        var mob = new Probe();
        var player = new Player(mock(Level.class), BlockPos.ZERO, 0, new GameProfile(UUID.randomUUID(), "PortAbsorption")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
        };
        for (var entity : new net.minecraft.world.entity.LivingEntity[] {mob, player}) {
            entity.setAbsorptionAmount(10);
            assertEquals(0.0F, entity.getAbsorptionAmount());
            var capacity = entity.getAttribute(attribute);
            assertNotNull(capacity);
            capacity.setBaseValue(6.0);
            entity.setAbsorptionAmount(9);
            assertEquals(6.0F, entity.getAbsorptionAmount());
            entity.setAbsorptionAmount(-1);
            assertEquals(0.0F, entity.getAbsorptionAmount());
        }

        mob.setAbsorptionAmount(6);
        mob.capacity().setBaseValue(3);
        mob.tickPotionEffects();
        assertEquals(3.0F, mob.getAbsorptionAmount());
    }

    @Test
    void potionCapacityCombinesWithInnateShieldWithoutGrantingOrRemovingExtraPoints() {
        var entity = new Probe();
        entity.capacity().setBaseValue(10);
        entity.setAbsorptionAmount(6);
        assertTrue(entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0)));
        assertEquals(14.0, entity.capacity().getValue());
        assertEquals(6.0F, entity.getAbsorptionAmount());

        assertTrue(entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 2)));
        assertEquals(22.0, entity.capacity().getValue());
        assertEquals(6.0F, entity.getAbsorptionAmount());
        entity.setAbsorptionAmount(13);
        assertTrue(entity.removeEffect(MobEffects.ABSORPTION));
        assertEquals(10.0, entity.capacity().getValue());
        assertEquals(10.0F, entity.getAbsorptionAmount());

        entity.setAbsorptionAmount(0);
        assertTrue(entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0)));
        assertEquals(4.0F, entity.getAbsorptionAmount());
        entity.setAbsorptionAmount(0);
        entity.tickPotionEffects();
        assertFalse(entity.hasEffect(MobEffects.ABSORPTION));
        assertEquals(10.0, entity.capacity().getValue());
    }

    @Test
    void reloadPreservesNativeShieldPointsAndReconstructsOlderPotionCapacity() {
        var source = new Probe();
        source.capacity().setBaseValue(8);
        assertTrue(source.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1)));
        source.setAbsorptionAmount(3);
        var save = new CompoundTag();
        source.addAdditionalSaveData(save);

        var loaded = new Probe();
        loaded.readAdditionalSaveData(save);
        assertEquals(16.0, loaded.capacity().getValue());
        assertEquals(3.0F, loaded.getAbsorptionAmount());

        var legacy = save.copy();
        legacy.remove("Attributes");
        var older = new Probe();
        older.readAdditionalSaveData(legacy);
        assertEquals(8.0, older.capacity().getValue());
        assertEquals(3.0F, older.getAbsorptionAmount());

        var transientShield = new CompoundTag();
        transientShield.putFloat("AbsorptionAmount", 3);
        var rejoining = new Probe();
        rejoining.readAdditionalSaveData(transientShield);
        assertEquals(3.0F, rejoining.getAbsorptionAmount());
        rejoining.capacity().addTransientModifier(NamedAttributeModifiers.create(
                ResourceLocation.fromNamespaceAndPath("thaumaturge", "runic_shielding"), 5, AttributeModifier.Operation.ADDITION));
        rejoining.tickPotionEffects();
        assertEquals(3.0F, rejoining.getAbsorptionAmount());
    }

    private static final class Probe extends Zombie {
        Probe() {
            super(EntityType.ZOMBIE, mock(Level.class));
        }

        AttributeInstance capacity() {
            return getAttribute(TTAttributes.MAX_ABSORPTION.get());
        }

        void tickPotionEffects() {
            tickEffects();
        }
    }
}
