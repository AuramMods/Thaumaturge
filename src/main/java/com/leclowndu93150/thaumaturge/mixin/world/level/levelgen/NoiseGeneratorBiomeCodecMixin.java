package com.leclowndu93150.thaumaturge.mixin.world.level.levelgen;

import com.leclowndu93150.thaumaturge.content.world.biome.RegionalBiomeSource;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseBasedChunkGenerator.class)
abstract class NoiseGeneratorBiomeCodecMixin {
    @ModifyReturnValue(method = {"lambda$static$0", "m_254819_"}, at = @At("RETURN"), remap = false)
    private static BiomeSource thaumaturge$serializeOriginalBiomeSource(BiomeSource source) {
        return source instanceof RegionalBiomeSource regional ? regional.delegate() : source;
    }
}
