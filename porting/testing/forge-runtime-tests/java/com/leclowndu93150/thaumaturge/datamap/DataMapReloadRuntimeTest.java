package com.leclowndu93150.thaumaturge.datamap;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DataMapReloadRuntimeTest {
    private static final ResourceKey<Registry<String>> ROOT = ResourceKey.createRegistryKey(new ResourceLocation("port_map_reload", "values"));
    private static final ResourceKey<String> A = ResourceKey.create(ROOT, new ResourceLocation("port_map_reload", "a"));
    private static final ResourceKey<String> B = ResourceKey.create(ROOT, new ResourceLocation("port_map_reload", "b"));
    private static final TagKey<String> SELECTED = TagKey.create(ROOT, new ResourceLocation("port_map_reload", "selected"));
    private static final DataMapType<String, Integer> COUNTS = DataMapType.builder(new ResourceLocation("port_map_reload", "counts"), ROOT, Codec.INT).build();
    private static final DataMapType<String, Holder<String>> HOLDERS = DataMapType.builder(new ResourceLocation("port_map_reload", "holders"), ROOT, RegistryFixedCodec.create(ROOT)).build();
    private static final DataMapType<Item, Integer> FLAGS = DataMapType.builder(new ResourceLocation("port_map_reload", "flags"), Registries.ITEM, Codec.INT).build();
    @TempDir Path temporary;
    private final List<Runnable> restoreTags = new ArrayList<>();

    @BeforeAll static void registerSchemasForTheActualResourceConstructorHook() {
        DataMaps.CATALOG.register(COUNTS); DataMaps.CATALOG.register(HOLDERS); DataMaps.CATALOG.register(FLAGS);
    }
    @BeforeEach void retainNativeSharedTags() { BuiltInRegistries.REGISTRY.forEach(this::retainTags); }
    private <R> void retainTags(Registry<R> registry) {
        Map<TagKey<R>, List<Holder<R>>> saved = new java.util.LinkedHashMap<>();
        registry.getTags().forEach(pair -> saved.put(pair.getFirst(), pair.getSecond().stream().toList()));
        restoreTags.add(() -> registry.bindTags(saved));
    }
    @AfterEach void restoreNativeSharedState() {
        restoreTags.forEach(Runnable::run); Blocks.rebuildCache(); DataMaps.replace(BuiltInRegistries.ITEM, Map.of());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RegistryAccess.Frozen owner(String value) {
        var registry = new MappedRegistry<String>(ROOT, Lifecycle.stable());
        registry.register(A, value + "_a", Lifecycle.stable()); registry.register(B, value + "_b", Lifecycle.stable()); registry.freeze();
        List<Registry<?>> registries = new ArrayList<>();
        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registries().forEach(entry -> registries.add(entry.value()));
        for (RegistryDataLoader.RegistryData<?> data : RegistryDataLoader.WORLDGEN_REGISTRIES) {
            if (registries.stream().noneMatch(r -> r.key().equals(data.key()))) registries.add(new MappedRegistry((ResourceKey) data.key(), Lifecycle.stable()).freeze());
        }
        registries.add(registry); return new RegistryAccess.ImmutableRegistryAccess(registries).freeze();
    }
    private ReloadableServerResources resources(RegistryAccess.Frozen owner) {
        return new ReloadableServerResources(owner, FeatureFlags.VANILLA_SET, Commands.CommandSelection.DEDICATED, 2);
    }
    private Path pack(String name) throws Exception {
        Path directory = temporary.resolve(name); Files.createDirectories(directory);
        Files.writeString(directory.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":15,\"description\":\"Native data-map fixture\"}}"); return directory;
    }
    private void write(Path pack, String file, String data) throws Exception {
        Path path = pack.resolve(file); Files.createDirectories(path.getParent()); Files.writeString(path, data);
    }
    private void map(Path pack, DataMapType<?, ?> type, String data) throws Exception {
        write(pack, "data/" + type.id().getNamespace() + "/" + DataMapLoader.folder(type.registryKey().location()) + "/" + type.id().getPath() + ".json", data);
    }
    private MultiPackResourceManager manager(Path... paths) {
        List<PackResources> packs = new ArrayList<>();
        for (var path : paths) packs.add(new PathPackResources(path.getFileName().toString(), path, false));
        return new MultiPackResourceManager(PackType.SERVER_DATA, packs);
    }
    private DataMapLoader reload(ReloadableServerResources resources, RegistryAccess owner, boolean apply, Path... packs) throws Exception {
        var participants = resources.listeners().stream().filter(listener -> listener instanceof TagManager || listener instanceof DataMapLoader).toList();
        assertEquals(2, participants.size());
        var loader = ((ResourceDataMaps) resources).thaumaturge$dataMapLoader(); assertTrue(participants.contains(loader));
        try (var manager = manager(packs)) { reload(participants, manager, () -> { if (apply) resources.updateRegistryTags(owner); }); }
        return loader;
    }
    private void reload(List<PreparableReloadListener> listeners, MultiPackResourceManager manager, Runnable apply) throws Exception {
        var background = Executors.newFixedThreadPool(2, work -> new Thread(work, "port-map-prepare"));
        var game = Executors.newSingleThreadExecutor(work -> new Thread(work, "port-map-apply"));
        try {
            SimpleReloadInstance.of(manager, listeners, background, game, CompletableFuture.completedFuture(Unit.INSTANCE)).done().get(30, TimeUnit.SECONDS);
            game.submit(apply).get(30, TimeUnit.SECONDS);
        } finally { background.shutdownNow(); game.shutdownNow(); }
    }

    @Test
    void actualPacksNativeTagPipelineAndResourceMixinApplyInOrderAndNotifyAfterAllRoots() throws Exception {
        var owner = owner("owner"); var registry = owner.registryOrThrow(ROOT); var resources = resources(owner);
        Path low = pack("low"); Path high = pack("high");
        write(low, "data/port_map_reload/" + TagManager.getTagDir(ROOT) + "/selected.json", "{\"values\":[\"port_map_reload:b\"]}");
        write(low, "data/port_map_reload/tags/items/condition.json", "{\"values\":[\"minecraft:paper\"]}");
        map(low, COUNTS, "{\"values\":{\"#port_map_reload:selected\":1,\"port_map_reload:a\":3}}");
        map(high, COUNTS, "{\"values\":{\"port_map_reload:a\":{\"value\":7,\"replace\":true}}}");
        map(low, HOLDERS, "{\"values\":{\"port_map_reload:a\":\"port_map_reload:b\"}}");
        map(low, FLAGS, "{\"values\":{\"minecraft:paper\":{\"neoforge:conditions\":[{\"type\":\"neoforge:not\",\"value\":{\"type\":\"neoforge:tag_empty\",\"tag\":\"port_map_reload:condition\"}}],\"neoforge:value\":11}}}");
        var events = new ArrayList<ResourceKey<? extends Registry<?>>>();
        Consumer<DataMapsUpdatedEvent> listener = event -> {
            assertSame(owner, event.getRegistries()); assertEquals(DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD, event.getCause());
            assertEquals("port-map-apply", Thread.currentThread().getName());
            assertEquals(7, DataMaps.get(registry.getHolderOrThrow(A), COUNTS)); assertEquals(1, DataMaps.get(registry.getHolderOrThrow(B), COUNTS));
            assertEquals(11, DataMaps.get(Items.PAPER.builtInRegistryHolder(), FLAGS)); events.add(event.getRegistryKey());
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            DataMapLoader loader = reload(resources, owner, false, low, high);
            assertTrue(loader.diagnostics().isEmpty(), loader.diagnostics().toString());
            assertNull(DataMaps.get(registry.getHolderOrThrow(A), COUNTS)); assertTrue(events.isEmpty());
            assertFalse(registry.getTagOrEmpty(SELECTED).iterator().hasNext());
            var game = Executors.newSingleThreadExecutor(work -> new Thread(work, "port-map-apply"));
            try { game.submit(() -> resources.updateRegistryTags(owner)).get(30, TimeUnit.SECONDS); } finally { game.shutdownNow(); }
            assertEquals(Set.of(ROOT, Registries.ITEM), Set.copyOf(events)); assertEquals(2, events.size());
            assertSame(registry.getHolderOrThrow(B), DataMaps.get(registry.getHolderOrThrow(A), HOLDERS));
            assertTrue(registry.getHolderOrThrow(B).is(SELECTED));
        } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
    }

    @Test
    void aNewNativeResourceInstanceClearsRemovedFilesRatherThanRetainingOldMaps() throws Exception {
        var owner = owner("owner"); var registry = owner.registryOrThrow(ROOT); var low = pack("first");
        map(low, COUNTS, "{\"values\":{\"port_map_reload:a\":7}}"); map(low, FLAGS, "{\"values\":{\"minecraft:paper\":4}}");
        assertTrue(reload(resources(owner), owner, true, low).diagnostics().isEmpty());
        assertEquals(7, DataMaps.get(registry.getHolderOrThrow(A), COUNTS)); assertEquals(4, DataMaps.get(Items.PAPER.builtInRegistryHolder(), FLAGS));
        var empty = pack("empty"); assertTrue(reload(resources(owner), owner, true, empty).diagnostics().isEmpty());
        assertNull(DataMaps.get(registry.getHolderOrThrow(A), COUNTS)); assertNull(DataMaps.get(Items.PAPER.builtInRegistryHolder(), FLAGS));
    }

    @Test
    void differentNativeResourceOwnersResolveHoldersIndependentlyAndRejectCrossOwnerApplication() throws Exception {
        var first = owner("first"); var second = owner("second"); Path pack = pack("owners");
        map(pack, HOLDERS, "{\"values\":{\"port_map_reload:a\":\"port_map_reload:b\"}}");
        var firstLoader = reload(resources(first), first, false, pack);
        assertThrows(IllegalArgumentException.class, () -> firstLoader.apply(second));
        assertNull(DataMaps.get(second.registryOrThrow(ROOT).getHolderOrThrow(A), HOLDERS));
        firstLoader.apply(first); assertEquals("first_b", DataMaps.get(first.registryOrThrow(ROOT).getHolderOrThrow(A), HOLDERS).value());
        reload(resources(second), second, true, pack);
        assertEquals("first_b", DataMaps.get(first.registryOrThrow(ROOT).getHolderOrThrow(A), HOLDERS).value());
        assertEquals("second_b", DataMaps.get(second.registryOrThrow(ROOT).getHolderOrThrow(A), HOLDERS).value());
    }

    @Test
    void malformedHigherPacksAndUnknownRequiredEntriesAreReportedWithoutPartialLayerApplication() throws Exception {
        var owner = owner("owner"); var low = pack("valid"); var middle = pack("missing"); var high = pack("invalid");
        map(low, COUNTS, "{\"values\":{\"port_map_reload:a\":5}}");
        map(middle, COUNTS, "{\"values\":{\"port_map_reload:missing\":21}}");
        map(high, COUNTS, "{\"values\":{\"port_map_reload:b\":9,\"port_map_reload:a\":\"bad\"}}");
        var loader = reload(resources(owner), owner, true, low, middle, high);
        assertEquals(2, loader.diagnostics().size(), loader.diagnostics().toString());
        assertTrue(loader.diagnostics().stream().anyMatch(error -> error.contains("invalid") && error.contains("counts.json")));
        assertTrue(loader.diagnostics().stream().anyMatch(error -> error.contains("port_map_reload:missing")));
        assertEquals(5, DataMaps.get(owner.registryOrThrow(ROOT).getHolderOrThrow(A), COUNTS));
        assertNull(DataMaps.get(owner.registryOrThrow(ROOT).getHolderOrThrow(B), COUNTS));
    }

    @Test
    void cleanupReleasesSharedRegistryValuesAndOnlyTheSpecifiedDynamicOwner() throws Exception {
        var first = owner("first"); var second = owner("second"); var pack = pack("cleanup");
        map(pack, HOLDERS, "{\"values\":{\"port_map_reload:a\":\"port_map_reload:b\"}}");
        map(pack, FLAGS, "{\"values\":{\"minecraft:paper\":4}}");
        reload(resources(first), first, true, pack); reload(resources(second), second, true, pack);
        DataMaps.clear(first);
        assertNull(DataMaps.get(first.registryOrThrow(ROOT).getHolderOrThrow(A), HOLDERS));
        assertNull(DataMaps.get(Items.PAPER.builtInRegistryHolder(), FLAGS));
        assertEquals("second_b", DataMaps.get(second.registryOrThrow(ROOT).getHolderOrThrow(A), HOLDERS).value());
    }

    @Test
    void aFailingMergerLeavesEveryRegistryAtItsPreviousPublication() throws Exception {
        var owner = owner("owner"); var registry = owner.registryOrThrow(ROOT); var catalog = new DataMapCatalog();
        var failing = DataMapType.builder(new ResourceLocation("port_map_reload", "failing"), ROOT, Codec.INT)
                .merger((r, oldSource, oldValue, newSource, newValue) -> { throw new IllegalStateException("Deliberate addon merger failure"); }).build();
        catalog.register(failing); catalog.register(FLAGS); catalog.freeze();
        DataMaps.replace(registry, Map.of(failing, Map.of(A, 8))); DataMaps.replace(BuiltInRegistries.ITEM, Map.of(FLAGS, Map.of(Items.PAPER.builtInRegistryHolder().key(), 6)));
        var low = pack("merge-low"); var high = pack("merge-high");
        map(low, failing, "{\"values\":{\"port_map_reload:a\":1}}"); map(high, failing, "{\"values\":{\"port_map_reload:a\":2}}");
        map(low, FLAGS, "{\"values\":{\"minecraft:paper\":99}}");
        var loader = new DataMapLoader(owner, ICondition.IContext.EMPTY, catalog);
        try (var manager = manager(low, high)) { reload(List.of(loader), manager, () -> {}); }
        assertThrows(IllegalStateException.class, () -> loader.apply(owner));
        assertEquals(8, DataMaps.get(registry.getHolderOrThrow(A), failing)); assertEquals(6, DataMaps.get(Items.PAPER.builtInRegistryHolder(), FLAGS));
    }
}
