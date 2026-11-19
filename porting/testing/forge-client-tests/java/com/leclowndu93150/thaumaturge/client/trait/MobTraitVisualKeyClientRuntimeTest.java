package com.leclowndu93150.thaumaturge.client.trait;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintBroodTrait;
import com.leclowndu93150.thaumaturge.registry.ForgeRegistration;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import org.junit.jupiter.api.Test;

class MobTraitVisualKeyClientRuntimeTest {
    @Test
    void visualCallbacksAndTintsBindBeforeTheNativeTraitHolderExists() {
        var entries = DeferredRegister.create(MobTrait.REGISTRY_KEY, "port_trait_visual");
        var brood = entries.register("taint_brood", TaintBroodTrait::new);
        var key = ForgeRegistration.entryKey(MobTrait.REGISTRY_KEY, brood);
        assertFalse(brood.isPresent());

        MobTraitParticles callback = (mob, level, random, x, y, z) ->
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        MobTraitVisuals.registerParticles(key, callback);
        MobTraitVisuals.registerTint(key, 0xff285060);

        var other = ResourceKey.create(MobTrait.REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath("port_other_visual", "taint_brood"));
        var wrapper = new MappedRegistry<MobTrait>(MobTrait.REGISTRY_KEY, Lifecycle.stable());
        wrapper.register(key, new TaintBroodTrait(), Lifecycle.stable());
        wrapper.register(other, new TaintBroodTrait(), Lifecycle.stable());
        wrapper.freeze();

        var first = wrapper.getHolderOrThrow(key);
        var second = wrapper.getHolderOrThrow(other);
        assertSame(callback, MobTraitVisuals.particles(first));
        assertEquals(0xff285060, MobTraitVisuals.tint(first));
        assertNull(MobTraitVisuals.particles(second));
        assertEquals(-1, MobTraitVisuals.tint(second));
        var level = mock(Level.class);
        MobTraitVisuals.particles(first).spawn(mock(LivingEntity.class), level, RandomSource.create(1), 2.0, 3.0, 4.0);
        verify(level).addParticle(ParticleTypes.SMOKE, 2.0, 3.0, 4.0, 0.0, 0.0, 0.0);
    }
}
