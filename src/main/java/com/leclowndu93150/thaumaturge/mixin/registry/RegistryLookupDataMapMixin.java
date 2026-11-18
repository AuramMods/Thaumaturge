package com.leclowndu93150.thaumaturge.mixin.registry;

import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.RegistryDataMapAccess;
import com.leclowndu93150.thaumaturge.datamap.RegistryDataMapLookup;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Native reference holders own this lookup view, which delegates to the same registry storage. */
@Mixin(targets = "net.minecraft.core.MappedRegistry$1")
abstract class RegistryLookupDataMapMixin<R> implements RegistryDataMapLookup<R> {
    @Unique private RegistryDataMapAccess<R> thaumaturge$storage;

    @Override public void thaumaturge$bindDataMapStorage(RegistryDataMapAccess<R> registry) {
        thaumaturge$storage = registry;
    }

    @Override public <T> Map<ResourceKey<R>, T> thaumaturge$dataMap(DataMapType<R, T> type) {
        return thaumaturge$storage.thaumaturge$dataMap(type);
    }

    @Override public void thaumaturge$replaceDataMaps(Map<DataMapType<R, ?>, Map<ResourceKey<R>, ?>> values) {
        thaumaturge$storage.thaumaturge$replaceDataMaps(values);
    }
}
