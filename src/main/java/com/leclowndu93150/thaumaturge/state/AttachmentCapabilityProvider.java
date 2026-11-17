package com.leclowndu93150.thaumaturge.state;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/** A Forge provider whose storage survives temporary invalidation during player cloning. */
final class AttachmentCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final Capability<AttachmentStorage> capability;
    private final AttachmentStorage storage;
    private LazyOptional<AttachmentStorage> value;

    AttachmentCapabilityProvider(Capability<AttachmentStorage> capability, AttachmentStorage storage) {
        this.capability = capability;
        this.storage = storage;
        this.value = LazyOptional.of(() -> storage);
    }

    @Override
    public synchronized <T> LazyOptional<T> getCapability(Capability<T> requested, Direction side) {
        if (requested != capability) return LazyOptional.empty();
        // CapabilityProvider rejects queries while its host is invalid. After
        // reviveCaps(), Forge delegates again, but the old optional stays invalid.
        if (!value.isPresent()) value = LazyOptional.of(() -> storage);
        return value.cast();
    }

    synchronized void invalidate() { value.invalidate(); }

    @Override public CompoundTag serializeNBT() { return storage.save(); }
    @Override public void deserializeNBT(CompoundTag tag) { storage.load(tag); }
}
