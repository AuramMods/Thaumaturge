package com.leclowndu93150.thaumaturge.content.world.biome;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class RegionalBiomeSource extends BiomeSource {
    private final BiomeSource delegate;
    private final Map<Holder<Biome>, BiomeChoices> choices;
    private final List<Holder<Biome>> addedBiomes;
    private final long seed;
    private final NormalNoise offset;
    private final NormalNoise shift;

    RegionalBiomeSource(BiomeSource delegate, Map<Holder<Biome>, BiomeChoices> choices, long seed, RandomState random) {
        this.delegate = delegate;
        this.choices = Map.copyOf(choices);
        this.addedBiomes = choices.values().stream()
                .flatMap(options -> options.regions().stream())
                .map(RegionChoice::biome)
                .distinct()
                .toList();
        this.seed = seed;
        this.offset = random.getOrCreateNoise(OverworldBiomeRegions.REGION_OFFSET);
        this.shift = random.getOrCreateNoise(Noises.SHIFT);
    }

    public BiomeSource delegate() {
        return delegate;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        // The noise generator's codec serializes the delegate. Rebuild the runtime view after loading.
        throw new UnsupportedOperationException("Regional biome source is only installed at world startup");
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(delegate.possibleBiomes().stream(), addedBiomes.stream());
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        Holder<Biome> original = delegate.getNoiseBiome(quartX, quartY, quartZ, sampler);
        BiomeChoices options = choices.get(original);
        if (options == null) {
            return original;
        }
        int x = QuartPos.toBlock(quartX);
        int z = QuartPos.toBlock(quartZ);
        double offsetValue = offset.getValue(x * 0.5, 0.0, z * 0.5) * 30.0;
        double shiftX = shift.getValue(x * 0.25, 0.0, z * 0.25) * 4.0;
        double shiftZ = shift.getValue(z * 0.25, x * 0.25, 0.0) * 4.0;
        double value = BiomeRegionCells.sample(seed, x + (offsetValue + shiftX), z + (offsetValue + shiftZ));
        return options.select(original, value);
    }

    record RegionChoice(int lowerBound, Holder<Biome> biome) {}

    record BiomeChoices(int totalWeight, List<RegionChoice> regions) {
        Holder<Biome> select(Holder<Biome> original, double sample) {
            int draw = (int) (Mth.clamp(sample, 0.0, 1.0) * totalWeight) + 1;
            for (int index = regions.size() - 1; index >= 0; index--) {
                RegionChoice region = regions.get(index);
                if (draw >= region.lowerBound()) {
                    return region.biome();
                }
            }
            return original;
        }
    }
}
