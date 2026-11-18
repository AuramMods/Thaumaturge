package com.leclowndu93150.thaumaturge.datamap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Registered schemas contain no world values or registry-owner references. */
public final class DataMapCatalog {
    private final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, DataMapType<?, ?>>> types = new LinkedHashMap<>();
    private boolean frozen;

    public synchronized void register(DataMapType<?, ?> type) {
        if (frozen) throw new IllegalStateException("Data map registration is closed");
        var registry = types.computeIfAbsent(type.registryKey(), key -> new LinkedHashMap<>());
        if (registry.putIfAbsent(type.id(), type) != null) throw new IllegalArgumentException("Duplicate data map " + type.registryKey().location() + "/" + type.id());
    }
    public synchronized void freeze() { frozen = true; }
    public synchronized DataMapType<?, ?> get(ResourceKey<? extends Registry<?>> registry, ResourceLocation id) {
        return types.getOrDefault(registry, Map.of()).get(id);
    }
    public synchronized List<DataMapType<?, ?>> types() {
        var result = new ArrayList<DataMapType<?, ?>>(); types.values().forEach(group -> result.addAll(group.values())); return List.copyOf(result);
    }
}
