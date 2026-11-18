package com.leclowndu93150.thaumaturge.datamap;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Native registry storage and mod-bus lifecycle for owned data maps. */
public final class DataMaps {
    public static final DataMapCatalog CATALOG = new DataMapCatalog();
    private DataMaps() {}

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> { ModLoader.get().postEvent(new RegisterDataMapTypesEvent(CATALOG)); CATALOG.freeze(); });
    }

    @SuppressWarnings("unchecked")
    public static <R, T> T get(Holder<R> holder, DataMapType<R, T> type) {
        return holder instanceof HolderDataMapAccess<?> access ? ((HolderDataMapAccess<R>) access).thaumaturge$data(type) : null;
    }

    @SuppressWarnings("unchecked")
    public static <R> boolean loaded(Holder<R> holder, DataMapType<R, ?> type) {
        return holder instanceof HolderDataMapAccess<?> access && ((HolderDataMapAccess<R>) access).thaumaturge$hasDataMap(type);
    }

    @SuppressWarnings("unchecked")
    public static <R> void replace(Registry<R> registry, Map<DataMapType<R, ?>, Map<ResourceKey<R>, ?>> values) {
        for (var entry : values.entrySet()) {
            if (!entry.getKey().registryKey().equals(registry.key())) throw new IllegalArgumentException("Data map belongs to another registry: " + entry.getKey().id());
            for (var key : entry.getValue().keySet()) {
                if (!key.isFor(registry.key()) || registry.getHolder(key).isEmpty()) throw new IllegalArgumentException("Unknown data map entry " + key);
            }
        }
        ((RegistryDataMapAccess<R>) registry).thaumaturge$replaceDataMaps(values);
    }

    /** Release dynamic holder values retained by shared built-in registries when their world closes. */
    public static void clear(RegistryAccess registries) {
        registries.registries().forEach(entry -> replace(entry.value(), Map.of()));
    }
}
