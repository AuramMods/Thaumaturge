package com.leclowndu93150.thaumaturge.data;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeDataGenerationRuntimeTest {
    private static final String NAMESPACE = "port_datagen";
    private static final ResourceKey<BiomeModifier> FEATURES =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, id("features"));
    private static final ResourceKey<BiomeModifier> SPAWNS =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, id("spawns"));

    @BeforeAll
    static void registerForgeBiomeModifierCodecs() throws ReflectiveOperationException {
        // The focused launcher skips the Forge mod's datapack registry event.
        var datapack = new DataPackRegistryEvent.NewRegistry();
        datapack.dataPackRegistry(ForgeRegistries.Keys.BIOME_MODIFIERS, BiomeModifier.DIRECT_CODEC);
        var process = DataPackRegistryEvent.NewRegistry.class.getDeclaredMethod("process");
        process.setAccessible(true);
        process.invoke(datapack);

        var field = ForgeMod.class.getDeclaredField("BIOME_MODIFIER_SERIALIZERS");
        field.setAccessible(true);
        var bus = BusBuilder.builder().build();
        ((DeferredRegister<?>) field.get(null)).register(bus);
        var registry = (ForgeRegistry<?>) ForgeRegistries.BIOME_MODIFIER_SERIALIZERS.get();
        var constructor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        constructor.setAccessible(true);
        registry.unfreeze();
        try {
            bus.post(constructor.newInstance(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, registry, null));
        } finally {
            registry.freeze();
        }
    }

    @Test
    void datagenSavesForgeBiomeModifiersAndSuppliesThePatchedLookupToLoot(@TempDir Path directory) throws Exception {
        var generator = new DataGenerator(directory, SharedConstants.getCurrentVersion(), true);
        var output = generator.getPackOutput();
        var vanilla = CompletableFuture.completedFuture(VanillaRegistries.createLookup());
        var entries = new RegistrySetBuilder().add(ForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
            var plains = HolderSet.direct(context.lookup(Registries.BIOME).getOrThrow(Biomes.PLAINS));
            var diamond = HolderSet.direct(context.lookup(Registries.PLACED_FEATURE).getOrThrow(OrePlacements.ORE_DIAMOND));
            context.register(FEATURES, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    plains, diamond, GenerationStep.Decoration.UNDERGROUND_ORES));
            context.register(SPAWNS, new ForgeBiomeModifiers.AddSpawnsBiomeModifier(
                    plains, List.of(new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 5, 1, 2))));
        });
        var lookup = generator.addProvider(true, new DatapackBuiltinEntriesProvider(
                output, vanilla, entries, Set.of(NAMESPACE))).getRegistryProvider();
        generator.addProvider(true, new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(() -> {
                    var value = lookup.join().lookupOrThrow(ForgeRegistries.Keys.BIOME_MODIFIERS)
                            .getOrThrow(FEATURES).value();
                    assertInstanceOf(ForgeBiomeModifiers.AddFeaturesBiomeModifier.class, value);
                    return register -> register.accept(id("fixture"), LootTable.lootTable()
                            .withPool(LootPool.lootPool().add(LootItem.lootTableItem(Blocks.STONE))));
                }, LootContextParamSets.CHEST))));

        generator.run();

        var features = read(directory.resolve("data/port_datagen/forge/biome_modifier/features.json"));
        assertEquals("forge:add_features", features.get("type").getAsString());
        assertEquals("underground_ores", features.get("step").getAsString());
        var spawns = read(directory.resolve("data/port_datagen/forge/biome_modifier/spawns.json"));
        assertEquals("forge:add_spawns", spawns.get("type").getAsString());
        assertTrue(spawns.get("spawners").toString().contains("minecraft:rabbit"));
        var loot = read(directory.resolve("data/port_datagen/loot_tables/fixture.json"));
        assertEquals("minecraft:chest", loot.get("type").getAsString());
        assertTrue(loot.get("pools").toString().contains("minecraft:stone"));
    }

    @Test
    void itemTagsCopyTheBlockProvidersGeneratedContents(@TempDir Path directory) throws Exception {
        var generator = new DataGenerator(directory, SharedConstants.getCurrentVersion(), true);
        var output = generator.getPackOutput();
        var lookup = CompletableFuture.completedFuture(VanillaRegistries.createLookup());
        var blockTag = TagKey.create(Registries.BLOCK, id("rock"));
        var itemTag = TagKey.create(Registries.ITEM, id("rock"));
        var blockTags = generator.addProvider(true, new BlockTagsProvider(output, lookup, NAMESPACE, null) {
            @Override
            protected void addTags(HolderLookup.Provider registries) {
                tag(blockTag).add(Blocks.STONE);
            }
        });
        generator.addProvider(true, new ItemTagsProvider(output, lookup, blockTags.contentsGetter(), NAMESPACE, null) {
            @Override
            protected void addTags(HolderLookup.Provider registries) {
                copy(blockTag, itemTag);
            }
        });

        generator.run();

        var block = read(directory.resolve("data/port_datagen/tags/blocks/rock.json"));
        var item = read(directory.resolve("data/port_datagen/tags/items/rock.json"));
        assertEquals("minecraft:stone", block.getAsJsonArray("values").get(0).getAsString());
        assertEquals(block, item);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static JsonObject read(Path path) throws Exception {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
