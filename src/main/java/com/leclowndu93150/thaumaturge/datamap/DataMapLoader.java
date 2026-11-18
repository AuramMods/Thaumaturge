package com.leclowndu93150.thaumaturge.datamap;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.slf4j.Logger;

/** One loader per ReloadableServerResources; no decoded data survives through a static cache. */
public final class DataMapLoader implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RegistryAccess owner;
    private final ICondition.IContext conditions;
    private final DataMapCatalog catalog;
    private List<Parsed<?, ?>> prepared;
    private final List<String> diagnostics = new ArrayList<>();
    private record Raw(ResourceLocation path, String pack, JsonElement json) {}
    private record Parsed<R, T>(DataMapType<R, T> type, List<DataMapFile<R, T>> files) {}
    private record Change<R>(Registry<R> registry, Map<DataMapType<R, ?>, Map<ResourceKey<R>, ?>> values) {}

    public DataMapLoader(RegistryAccess owner, ICondition.IContext conditions, DataMapCatalog catalog) {
        this.owner = owner; this.conditions = conditions; this.catalog = catalog;
    }
    public List<String> diagnostics() { return List.copyOf(diagnostics); }
    public static String folder(ResourceLocation registry) {
        return "data_maps/" + (registry.getNamespace().equals("minecraft") ? "" : registry.getNamespace() + "/") + registry.getPath();
    }

    @Override public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
            ProfilerFiller preparationProfiler, ProfilerFiller reloadProfiler, Executor background, Executor game) {
        diagnostics.clear(); prepared = null;
        return CompletableFuture.supplyAsync(() -> read(manager), background).thenCompose(barrier::wait)
                .thenAcceptAsync(raw -> {
                    var parsed = new ArrayList<Parsed<?, ?>>();
                    raw.forEach((type, files) -> parsed.add(decode(type, files)));
                    prepared = List.copyOf(parsed);
                }, game);
    }

    private Map<DataMapType<?, ?>, List<Raw>> read(ResourceManager manager) {
        Map<DataMapType<?, ?>, List<Raw>> result = new LinkedHashMap<>();
        catalog.types().forEach(type -> result.put(type, new ArrayList<>()));
        owner.registries().forEach(entry -> {
            var converter = FileToIdConverter.json(folder(entry.key().location()));
            converter.listMatchingResourceStacks(manager).forEach((path, resources) -> {
                var type = catalog.get(entry.key(), converter.fileToId(path));
                if (type == null) { LOGGER.warn("Unknown data map {} for registry {}", converter.fileToId(path), entry.key().location()); return; }
                for (var resource : resources) try (var reader = resource.openAsReader()) {
                    result.get(type).add(new Raw(path, resource.sourcePackId(), JsonParser.parseReader(reader)));
                } catch (IOException | JsonParseException failure) { error(path + " from " + resource.sourcePackId() + ": " + failure.getMessage(), failure); }
            });
        });
        return result;
    }

    private <R, T> Parsed<R, T> decode(DataMapType<R, T> type, List<Raw> files) {
        List<DataMapFile<R, T>> result = new ArrayList<>();
        var ops = RegistryOps.create(JsonOps.INSTANCE, owner);
        for (var file : files) try { result.add(DataMapFile.read(type, ops, file.json(), conditions)); }
        catch (JsonParseException failure) { error(file.path() + " from " + file.pack() + ": " + failure.getMessage(), failure); }
        return new Parsed<>(type, List.copyOf(result));
    }

    /** Called after native tag rebinding, once all resource listeners have finished. */
    public void apply(RegistryAccess registries) {
        if (prepared == null) throw new IllegalStateException("Data maps have not completed resource preparation");
        List<Change<?>> changes = new ArrayList<>();
        registries.registries().forEach(entry -> stage(entry.value(), changes));
        changes.forEach(this::publish);
        changes.forEach(change -> MinecraftForge.EVENT_BUS.post(new DataMapsUpdatedEvent(registries, change.registry(), DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD)));
        prepared = null;
    }

    @SuppressWarnings("unchecked")
    private <R> void stage(Registry<R> registry, List<Change<?>> changes) {
        Map<DataMapType<R, ?>, Map<ResourceKey<R>, ?>> values = new LinkedHashMap<>();
        for (var parsed : prepared) if (parsed.type().registryKey().equals(registry.key())) {
            if (owner.registryOrThrow(registry.key()) != registry) throw new IllegalArgumentException("Data map registry owner changed during reload: " + registry.key());
            var typed = (Parsed<R, ?>) parsed;
            values.put(typed.type(), build(registry, typed));
        }
        if (!values.isEmpty()) changes.add(new Change<>(registry, values));
    }
    private <R, T> Map<ResourceKey<R>, T> build(Registry<R> registry, Parsed<R, T> parsed) {
        return DataMapAssembler.build(registry, parsed.type(), parsed.files(), message -> error(parsed.type().id() + ": " + message, null));
    }
    private <R> void publish(Change<R> change) { DataMaps.replace(change.registry(), change.values()); }
    private void error(String message, Throwable failure) {
        diagnostics.add(message);
        LOGGER.error("Data map error: " + message, failure);
    }
}
