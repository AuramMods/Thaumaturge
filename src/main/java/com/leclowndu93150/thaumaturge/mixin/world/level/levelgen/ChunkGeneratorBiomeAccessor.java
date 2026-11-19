package com.leclowndu93150.thaumaturge.mixin.world.level.levelgen;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorBiomeAccessor {
    @Mutable
    @Accessor("biomeSource")
    void thaumaturge$setBiomeSource(BiomeSource source);

    @Mutable
    @Accessor("featuresPerStep")
    void thaumaturge$setFeaturesPerStep(Supplier<List<FeatureSorter.StepFeatureData>> features);
}
