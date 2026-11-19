package com.leclowndu93150.thaumaturge.content.world.biome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.Test;

class BiomeRegionTest {
    @Test
    void cellularValuesMatchLithostitchedReference() {
        // These are hexadecimal double outputs from the 1.8.0 Overworld FastNoise Lite configuration.
        assertEquals(0x1.c38e8ap-1, BiomeRegionCells.sample(0L, 0.0, 0.0));
        assertEquals(0x1.c90214p-1, BiomeRegionCells.sample(206L, 244.5, -320.0));
        assertEquals(0x1.eadadbp-1, BiomeRegionCells.sample(-4711L, -812.0, -49.8));
        assertEquals(0x1.e1215p-4, BiomeRegionCells.sample(1_000_000_000L, 12557.0, 21385.64));
        assertEquals(0x1.14c988p-3, BiomeRegionCells.sample(Long.MIN_VALUE, -27_400_000.0, 3_710_000.0));
    }

    @Test
    void overlappingRegionsUseTheOriginalWeightBoundaries() {
        var biomes = VanillaRegistries.createLookup().lookupOrThrow(Registries.BIOME);
        var vanilla = biomes.getOrThrow(Biomes.FOREST);
        var magical = biomes.getOrThrow(Biomes.FLOWER_FOREST);
        var tainted = biomes.getOrThrow(Biomes.SWAMP);
        var options = new RegionalBiomeSource.BiomeChoices(170, List.of(
                new RegionalBiomeSource.RegionChoice(101, magical),
                new RegionalBiomeSource.RegionChoice(161, tainted)));

        assertSame(vanilla, options.select(vanilla, 99.5 / 170));
        assertSame(magical, options.select(vanilla, 100.0 / 170));
        assertSame(magical, options.select(vanilla, 159.5 / 170));
        assertSame(tainted, options.select(vanilla, 160.0 / 170));
        assertSame(tainted, options.select(vanilla, 1.0));
    }
}
