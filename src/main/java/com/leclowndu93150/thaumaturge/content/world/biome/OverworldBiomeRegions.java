package com.leclowndu93150.thaumaturge.content.world.biome;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.mixin.world.level.levelgen.ChunkGeneratorBiomeAccessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** Applies Thaumaturge's Lithostitched region assets on the Forge version predating biome injectors. */
@EventBusSubscriber(modid = TTIds.MODID)
public final class OverworldBiomeRegions {
    static final ResourceKey<NormalNoise.NoiseParameters> REGION_OFFSET =
            ResourceKey.create(Registries.NOISE, new ResourceLocation("lithostitched", "region/offset"));
    private static final ResourceLocation PARTIAL_REPLACEMENT = new ResourceLocation("lithostitched", "replace_partially");
    private static final int DEFAULT_REGION_WEIGHT = 100;
    private static final int DEFAULT_PRIORITY = 1000;

    private OverworldBiomeRegions() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void install(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        LevelStem overworld = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM).get(LevelStem.OVERWORLD);
        if (overworld == null || !(overworld.generator() instanceof NoiseBasedChunkGenerator generator)) {
            return;
        }
        BiomeSource source = generator.getBiomeSource();
        if (source instanceof RegionalBiomeSource || source instanceof FixedBiomeSource
                || source instanceof CheckerboardColumnBiomeSource) {
            return;
        }

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, server.registryAccess());
        var regions = read(server.getResourceManager(), ops, "lithostitched/region", Region.CODEC);
        var replacements = read(server.getResourceManager(), ops, "lithostitched/biome_injector", Replacement.CODEC)
                .values().stream()
                .filter(replacement -> replacement.dimension().equals(LevelStem.OVERWORLD))
                .sorted(Comparator.comparingInt(Replacement::priority))
                .toList();
        var choices = new LinkedHashMap<Holder<Biome>, RegionalBiomeSource.BiomeChoices>();
        for (Holder<Biome> biome : source.possibleBiomes()) {
            RegionalBiomeSource.BiomeChoices plan = choicesFor(biome, regions, replacements);
            if (plan.regions().stream().anyMatch(choice -> !choice.biome().equals(biome))) {
                choices.put(biome, plan);
            }
        }
        if (choices.isEmpty()) {
            return;
        }
        long seed = server.getWorldData().worldGenOptions().seed();
        var noise = RandomState.create(generator.generatorSettings().value(),
                server.registryAccess().lookupOrThrow(Registries.NOISE), seed);
        var regional = new RegionalBiomeSource(source, choices, seed, noise);
        var access = (ChunkGeneratorBiomeAccessor) generator;
        access.thaumaturge$setBiomeSource(regional);
        access.thaumaturge$setFeaturesPerStep(Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(
                List.copyOf(regional.possibleBiomes()), biome -> generator.getBiomeGenerationSettings(biome).features(),
                true)));
    }

    private static RegionalBiomeSource.BiomeChoices choicesFor(
            Holder<Biome> biome, Map<ResourceLocation, Region> regions, List<Replacement> replacements) {
        // Lithostitched sorts the default region before Thaumaturge's regions and weighs it only for Overworld biomes.
        int next = biome.is(BiomeTags.IS_OVERWORLD) ? DEFAULT_REGION_WEIGHT + 1 : 1;
        var choices = new ArrayList<RegionalBiomeSource.RegionChoice>();
        for (var entry : regions.entrySet()) {
            Region region = entry.getValue();
            if (!region.dimension().equals(LevelStem.OVERWORLD) || region.weight() == 0 || !region.biomes().contains(biome)) {
                continue;
            }
            Holder<Biome> replacement = biome;
            for (Replacement candidate : replacements) {
                if (candidate.region().equals(entry.getKey()) && candidate.targets().contains(biome)) {
                    replacement = candidate.replacement();
                    break;
                }
            }
            choices.add(new RegionalBiomeSource.RegionChoice(next, replacement));
            next = Math.addExact(next, region.weight());
        }
        return new RegionalBiomeSource.BiomeChoices(next - 1, List.copyOf(choices));
    }

    private static <T> Map<ResourceLocation, T> read(
            ResourceManager resources, RegistryOps<JsonElement> ops, String folder, Codec<T> codec) {
        var files = FileToIdConverter.json(folder);
        var values = new TreeMap<ResourceLocation, T>();
        for (var entry : files.listMatchingResources(resources).entrySet()) {
            ResourceLocation path = entry.getKey();
            if (!path.getNamespace().equals(TTIds.MODID)) {
                continue;
            }
            try (var reader = entry.getValue().openAsReader()) {
                T value = codec.parse(ops, JsonParser.parseReader(reader))
                        .getOrThrow(false, reason -> { throw new IllegalStateException(path + ": " + reason); });
                values.put(files.fileToId(path), value);
            } catch (IOException error) {
                throw new IllegalStateException("Cannot read biome data " + path, error);
            }
        }
        return values;
    }

    private record Region(ResourceKey<LevelStem> dimension, HolderSet<Biome> biomes, int weight) {
        private static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.LEVEL_STEM).fieldOf("dimension").forGetter(Region::dimension),
                Biome.LIST_CODEC.fieldOf("biomes").forGetter(Region::biomes),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weight").forGetter(Region::weight)
        ).apply(instance, Region::new));
    }

    private record Replacement(ResourceLocation type, ResourceKey<LevelStem> dimension, HolderSet<Biome> targets,
                               Holder<Biome> replacement, ResourceLocation region, int priority,
                               Map<String, JsonElement> parameters) {
        private static final Codec<Replacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("type").forGetter(Replacement::type),
                ResourceKey.codec(Registries.LEVEL_STEM).fieldOf("dimension").forGetter(Replacement::dimension),
                Biome.LIST_CODEC.fieldOf("targets").forGetter(Replacement::targets),
                Biome.CODEC.fieldOf("replacement").forGetter(Replacement::replacement),
                ResourceLocation.CODEC.fieldOf("region").forGetter(Replacement::region),
                Codec.INT.optionalFieldOf("priority", DEFAULT_PRIORITY).forGetter(Replacement::priority),
                Codec.unboundedMap(Codec.STRING, ExtraCodecs.JSON).optionalFieldOf("parameters", Map.of())
                        .forGetter(Replacement::parameters)
        ).apply(instance, Replacement::new));

        private Replacement {
            if (!type.equals(PARTIAL_REPLACEMENT) || !parameters.isEmpty()) {
                throw new IllegalArgumentException("Unsupported regional biome injector " + type);
            }
        }
    }
}
