package com.leclowndu93150.thaumaturge.itemdata;

import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/** A typed, namespaced field in an item's ordinary 1.20.1 NBT. */
public final class ItemDataKey<T> {
    private final ResourceLocation id;
    private final Codec<T> codec;
    private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

    ItemDataKey(ResourceLocation id, Builder<T> builder) {
        this.id = id;
        this.codec = Objects.requireNonNull(builder.codec, "Item data needs a persistent codec: " + id);
        this.streamCodec = builder.streamCodec;
    }

    public ResourceLocation id() { return id; }
    public Codec<T> codec() { return codec; }
    public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() { return streamCodec; }

    public Tag encode(DynamicOps<Tag> ops, T value) {
        return codec.encodeStart(ops, Objects.requireNonNull(value, "Item data value"))
                .getOrThrow(false, message -> { throw new IllegalArgumentException("Cannot encode " + id + ": " + message); });
    }

    public T decode(DynamicOps<Tag> ops, Tag tag) {
        return codec.parse(ops, tag)
                .getOrThrow(false, message -> { throw new IllegalArgumentException("Cannot decode " + id + ": " + message); });
    }

    public static <T> Builder<T> builder() { return new Builder<>(); }

    public static final class Builder<T> {
        private Codec<T> codec;
        private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

        public Builder<T> persistent(Codec<T> codec) {
            this.codec = Objects.requireNonNull(codec);
            return this;
        }

        public Builder<T> networkSynchronized(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            this.streamCodec = Objects.requireNonNull(streamCodec);
            return this;
        }
    }
}
