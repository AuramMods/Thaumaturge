package com.leclowndu93150.thaumaturge.mixin.registry;

import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.HolderDataMapAccess;
import com.leclowndu93150.thaumaturge.datamap.RegistryDataMapAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Holder.Reference.class)
abstract class HolderReferenceDataMapMixin<R> implements HolderDataMapAccess<R> {
    @Shadow @Final private HolderOwner<R> owner;
    @Shadow public abstract ResourceKey<R> key();

    @Override @SuppressWarnings("unchecked")
    public <T> T thaumaturge$data(DataMapType<R, T> type) {
        return owner instanceof RegistryDataMapAccess<?> storage
                ? ((RegistryDataMapAccess<R>) storage).thaumaturge$dataMap(type).get(key()) : null;
    }

    @Override @SuppressWarnings("unchecked")
    public boolean thaumaturge$hasDataMap(DataMapType<R, ?> type) {
        return owner instanceof RegistryDataMapAccess<?> storage
                && ((RegistryDataMapAccess<R>) storage).thaumaturge$hasDataMap(type);
    }
}
