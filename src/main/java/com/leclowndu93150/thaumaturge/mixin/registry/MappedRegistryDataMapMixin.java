package com.leclowndu93150.thaumaturge.mixin.registry;

import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.RegistryDataMapAccess;
import com.leclowndu93150.thaumaturge.datamap.RegistryDataMapLookup;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MappedRegistry.class)
abstract class MappedRegistryDataMapMixin<R> implements RegistryDataMapAccess<R> {
    @Shadow @Final private HolderLookup.RegistryLookup<R> lookup;
    @Unique private volatile Map<DataMapType<R, ?>, Map<ResourceKey<R>, ?>> thaumaturge$dataMaps = Map.of();

    @Inject(method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("RETURN"))
    @SuppressWarnings("unchecked")
    private void thaumaturge$bindLookupStorage(CallbackInfo ci) {
        ((RegistryDataMapLookup<R>) lookup).thaumaturge$bindDataMapStorage(this);
    }

    @Override @SuppressWarnings("unchecked")
    public <T> Map<ResourceKey<R>, T> thaumaturge$dataMap(DataMapType<R, T> type) {
        return (Map<ResourceKey<R>, T>) thaumaturge$dataMaps.getOrDefault(type, Map.of());
    }

    @Override public boolean thaumaturge$hasDataMap(DataMapType<R, ?> type) {
        return thaumaturge$dataMaps.containsKey(type);
    }

    @Override public void thaumaturge$replaceDataMaps(Map<DataMapType<R, ?>, Map<ResourceKey<R>, ?>> values) {
        Map<DataMapType<R, ?>, Map<ResourceKey<R>, ?>> copy = new LinkedHashMap<>();
        values.forEach((type, data) -> copy.put(type, Map.copyOf(data)));
        thaumaturge$dataMaps = Map.copyOf(copy);
    }
}
