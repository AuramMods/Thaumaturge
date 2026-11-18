package com.leclowndu93150.thaumaturge.datamap;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.FalseCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DataMapProviderRuntimeTest {
    private static final ResourceKey<Registry<String>> ROOT = ResourceKey.createRegistryKey(new ResourceLocation("port_map_generate", "values"));
    private static final ResourceKey<String> A = ResourceKey.create(ROOT, new ResourceLocation("port_map_generate", "a"));
    private static final ResourceKey<String> B = ResourceKey.create(ROOT, new ResourceLocation("port_map_generate", "b"));
    private static final TagKey<String> TAG = TagKey.create(ROOT, new ResourceLocation("port_map_generate", "selected"));
    private static final DataMapType<Item, Integer> PLAIN = DataMapType.builder(new ResourceLocation("port_map_generate", "plain"), Registries.ITEM, Codec.INT).build();
    private static final DataMapType<String, Holder<String>> HOLDERS = DataMapType.builder(new ResourceLocation("port_map_generate", "nested/holders"), ROOT, RegistryFixedCodec.create(ROOT)).build();
    private static final DataMapType<Item, Integer> DISABLED = DataMapType.builder(new ResourceLocation("port_map_generate", "disabled"), Registries.ITEM, Codec.INT).build();
    @TempDir Path output;

    private RegistryAccess.Frozen owner(String prefix) {
        var registry = new MappedRegistry<String>(ROOT, Lifecycle.stable());
        registry.register(A, prefix + "_a", Lifecycle.stable()); registry.register(B, prefix + "_b", Lifecycle.stable()); registry.freeze();
        registry.bindTags(Map.of(TAG, List.of(registry.getHolderOrThrow(A), registry.getHolderOrThrow(B))));
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registry, BuiltInRegistries.ITEM)).freeze();
    }
    private DataMapProvider provider(HolderLookup.Provider owner, Consumer<DataMapProvider> gather) {
        return new DataMapProvider(new PackOutput(output), CompletableFuture.completedFuture(owner)) {
            @Override protected void gather(HolderLookup.Provider registries) { assertSame(owner, registries); gather.accept(this); }
        };
    }
    private Path file(DataMapType<?, ?> type) { return output.resolve("data").resolve(type.id().getNamespace()).resolve(DataMapLoader.folder(type.registryKey().location())).resolve(type.id().getPath() + ".json"); }
    private JsonElement read(DataMapType<?, ?> type) throws Exception { return JsonParser.parseString(Files.readString(file(type))); }
    private DataMapLoader load(RegistryAccess owner, DataMapType<?, ?>... types) throws Exception {
        var catalog = new DataMapCatalog(); for (var type : types) catalog.register(type); catalog.freeze();
        var loader = new DataMapLoader(owner, ICondition.IContext.EMPTY, catalog);
        Files.writeString(output.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":15,\"description\":\"Native datagen fixture\"}}");
        try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(new PathPackResources("generated", output, false)))) {
            SimpleReloadInstance.of(manager, List.of(loader), Runnable::run, Runnable::run, CompletableFuture.completedFuture(Unit.INSTANCE)).done().join();
            loader.apply(owner);
        }
        assertTrue(loader.diagnostics().isEmpty(), loader.diagnostics().toString()); return loader;
    }

    @Test
    void generatedFilesRoundTripThroughNativePackReloadWithSelectorsConditionsAndStableCachedBytes() throws Exception {
        var owner = owner("owner"); var registry = owner.registryOrThrow(ROOT);
        var future = RegistryObject.create(new ResourceLocation("port_map_generate", "unbound"), ForgeRegistries.ITEMS);
        assertFalse(future.isPresent());
        var provider = provider(owner, generator -> {
            generator.builder(PLAIN).add(Items.PAPER.builtInRegistryHolder(), 7, false)
                    .add(Items.IRON_INGOT.builtInRegistryHolder().key(), 9, false)
                    .add(new ResourceLocation("minecraft", "diamond"), 11, false)
                    .add(future, 13, false, FalseCondition.INSTANCE).remove(Items.DIAMOND.builtInRegistryHolder());
            generator.builder(HOLDERS).add(TAG, registry.getHolderOrThrow(A), false)
                    .add(A, registry.getHolderOrThrow(B), true, TrueCondition.INSTANCE)
                    .remove(B.location()).conditions(TrueCondition.INSTANCE);
            generator.builder(DISABLED).add(Items.PAPER.builtInRegistryHolder(), 99, false).replace(true).conditions(FalseCondition.INSTANCE);
        });
        var hashes = new ConcurrentHashMap<Path, List<String>>();
        CachedOutput cache = (path, bytes, hash) -> {
            assertEquals(Hashing.sha1().hashBytes(bytes), hash);
            hashes.computeIfAbsent(path, key -> new ArrayList<>()).add(hash.toString());
            CachedOutput.NO_CACHE.writeIfNeeded(path, bytes, hash);
        };
        try {
            provider.run(cache).join(); var before = read(HOLDERS); provider.run(cache).join();
            assertEquals(before, read(HOLDERS)); assertEquals(3, hashes.size());
            hashes.values().forEach(values -> { assertEquals(2, values.size()); assertEquals(values.get(0), values.get(1)); });
            assertEquals(1, read(HOLDERS).getAsJsonObject().getAsJsonArray("remove").size());
            assertTrue(Files.exists(output.resolve("data/port_map_generate/data_maps/port_map_generate/values/nested/holders.json")));
            load(owner, PLAIN, HOLDERS, DISABLED);
            assertEquals(7, DataMaps.get(Items.PAPER.builtInRegistryHolder(), PLAIN)); assertEquals(9, DataMaps.get(Items.IRON_INGOT.builtInRegistryHolder(), PLAIN));
            assertNull(DataMaps.get(Items.DIAMOND.builtInRegistryHolder(), PLAIN)); assertNull(DataMaps.get(Items.PAPER.builtInRegistryHolder(), DISABLED));
            assertSame(registry.getHolderOrThrow(B), DataMaps.get(registry.getHolderOrThrow(A), HOLDERS));
            assertNull(DataMaps.get(registry.getHolderOrThrow(B), HOLDERS)); assertFalse(future.isPresent());
        } finally { DataMaps.clear(owner); }
    }

    @Test
    void actualInfernalBonusSchemaGeneratesTargetedAndWholeRemovalRecords() throws Exception {
        var owner = owner("owner"); var paper = InfernalBonus.builder(Items.PAPER).count(2).chance(0.5f).build(); var diamond = InfernalBonus.builder(Items.DIAMOND).build();
        var tag = TagKey.create(Registries.ITEM, new ResourceLocation("port_map_generate", "unused"));
        var provider = provider(owner, generator -> generator.builder(InfernalBonus.DATA_MAP)
                .add(Items.IRON_INGOT.builtInRegistryHolder(), List.of(paper, diamond), false)
                .remove(Items.IRON_INGOT.builtInRegistryHolder(), new InfernalBonus.Remover(Items.PAPER))
                .remove(new ResourceLocation("minecraft", "iron_ingot"), new InfernalBonus.Remover(Items.PAPER))
                .remove(tag, new InfernalBonus.Remover(Items.PAPER)).remove(tag).remove(new ResourceLocation("minecraft", "coal")));
        try {
            provider.run(CachedOutput.NO_CACHE).join();
            var encoded = read(InfernalBonus.DATA_MAP).getAsJsonObject(); assertEquals(5, encoded.getAsJsonArray("remove").size());
            assertEquals("minecraft:paper", encoded.getAsJsonArray("remove").get(0).getAsJsonObject().get("remover").getAsString());
            load(owner, InfernalBonus.DATA_MAP);
            var bonuses = DataMaps.get(Items.IRON_INGOT.builtInRegistryHolder(), InfernalBonus.DATA_MAP);
            assertEquals(1, bonuses.size()); assertTrue(bonuses.get(0).items().contains(Items.DIAMOND.builtInRegistryHolder()));
        } finally { DataMaps.clear(owner); }
    }

    @Test
    void holderEncodingUsesTheProvidedLookupAndFailsBeforeOverwritingAnExistingFile() throws Exception {
        var first = owner("first"); var second = owner("second");
        Files.createDirectories(file(HOLDERS).getParent()); Files.writeString(file(HOLDERS), "retained existing output");
        var writes = new AtomicInteger();
        var provider = provider(second, generator -> generator.builder(HOLDERS).add(A, first.registryOrThrow(ROOT).getHolderOrThrow(B), false));
        var failure = assertThrows(CompletionException.class, () -> provider.run((path, bytes, hash) -> writes.incrementAndGet()).join());
        assertTrue(failure.getCause().getMessage().contains(HOLDERS.id().toString())); assertEquals(0, writes.get());
        assertEquals("retained existing output", Files.readString(file(HOLDERS)));
    }

    @Test
    void actualCachedOutputWriteFailuresFailTheProviderFuture() {
        var owner = owner("owner"); var writes = new AtomicInteger(); var io = new IOException("Deliberate cached-output failure");
        var provider = provider(owner, generator -> generator.builder(PLAIN).add(Items.PAPER.builtInRegistryHolder(), 7, false));
        var failure = assertThrows(CompletionException.class, () -> provider.run((path, bytes, hash) -> { writes.incrementAndGet(); throw io; }).join());
        assertInstanceOf(UncheckedIOException.class, failure.getCause()); assertSame(io, failure.getCause().getCause()); assertEquals(1, writes.get());
    }

    @Test
    void falseWholeFileConditionsSkipInvalidPayloadsAndDoNotClearLowerLayers() {
        var registry = owner("owner").registryOrThrow(ROOT);
        var type = DataMapType.builder(new ResourceLocation("port_map_generate", "whole_file"), ROOT, Codec.INT).build();
        var low = DataMapFile.read(type, com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseString("{\"values\":{\"port_map_generate:a\":3}}"), ICondition.IContext.EMPTY);
        var skipped = DataMapFile.read(type, com.mojang.serialization.JsonOps.INSTANCE,
                JsonParser.parseString("{\"forge:conditions\":[{\"type\":\"forge:false\"}],\"replace\":true,\"values\":false}"), ICondition.IContext.EMPTY);
        assertEquals(Map.of(A, 3), DataMapAssembler.build(registry, type, List.of(low, skipped), message -> fail(message)));
    }
}
