package com.leclowndu93150.thaumaturge.datamap;

import java.util.Map;
import net.minecraft.resources.ResourceKey;

/** Implemented on native MappedRegistry and inherited by Forge's NamespacedWrapper. */
public interface RegistryDataMapAccess<R> {
    <T> Map<ResourceKey<R>, T> thaumaturge$dataMap(DataMapType<R, T> type);
    void thaumaturge$replaceDataMaps(Map<DataMapType<R, ?>, Map<ResourceKey<R>, ?>> values);
}
