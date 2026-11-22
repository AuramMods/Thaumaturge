package com.leclowndu93150.thaumaturge.mixin.world.level.levelgen;

import com.leclowndu93150.thaumaturge.content.world.biome.RegionalBiomeSource;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeSource.class)
abstract class BiomeSourceCodecMixin {
    @Shadow @Final @Mutable public static Codec<BiomeSource> CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void thaumaturge$serializeOriginalBiomeSource(CallbackInfo ci) {
        // Generators serialize the original source. The regional overlay is rebuilt at world startup.
        CODEC = CODEC.xmap(Function.identity(), source ->
                source instanceof RegionalBiomeSource regional ? regional.delegate() : source);
    }
}
