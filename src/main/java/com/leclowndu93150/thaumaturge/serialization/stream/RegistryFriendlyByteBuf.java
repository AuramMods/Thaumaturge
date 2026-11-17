package com.leclowndu93150.thaumaturge.serialization.stream;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;

/** A 1.20.1 buffer carrying the registries of the world/connection being encoded. */
public final class RegistryFriendlyByteBuf extends FriendlyByteBuf {
    private final HolderLookup.Provider registries;

    public RegistryFriendlyByteBuf(ByteBuf source, RegistryAccess registries) {
        this(source, (HolderLookup.Provider) registries);
    }

    public RegistryFriendlyByteBuf(ByteBuf source, HolderLookup.Provider registries) {
        super(source);
        this.registries = Objects.requireNonNull(registries);
    }

    public RegistryAccess registryAccess() {
        if (registries instanceof RegistryAccess access) return access;
        throw new IllegalStateException("Numeric registry IDs require RegistryAccess");
    }

    public HolderLookup.Provider registryLookup() { return registries; }
}
