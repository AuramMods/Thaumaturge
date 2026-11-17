package com.leclowndu93150.thaumaturge.state;

import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

/** Schema and lifecycle policy for one value in the mod's Forge state capability. */
public final class AttachmentKey<T> {
    private final ResourceLocation id;
    private final Supplier<T> factory;
    private final Codec<T> codec;
    private final Predicate<T> shouldSave;
    private final StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec;
    private final boolean ownerOnly;
    private final boolean copyOnDeath;

    AttachmentKey(ResourceLocation id, Builder<T> builder) {
        this.id = id;
        this.factory = builder.factory;
        this.codec = builder.codec;
        this.shouldSave = builder.shouldSave;
        this.syncCodec = builder.syncCodec;
        this.ownerOnly = builder.ownerOnly;
        this.copyOnDeath = builder.copyOnDeath;
    }

    public ResourceLocation id() { return id; }
    public T createDefault() { return factory.get(); }
    public Codec<T> codec() { return codec; }
    public boolean shouldSave(T value) { return codec != null && shouldSave.test(value); }
    public boolean persistent() { return codec != null; }
    public boolean copyOnDeath() { return copyOnDeath; }
    public boolean ownerOnly() { return ownerOnly; }
    public StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec() { return syncCodec; }

    public static <T> Builder<T> builder(Supplier<T> factory) { return new Builder<>(factory); }

    public static final class Builder<T> {
        private final Supplier<T> factory;
        private Codec<T> codec;
        private Predicate<T> shouldSave = value -> true;
        private StreamCodec<? super RegistryFriendlyByteBuf, T> syncCodec;
        private boolean ownerOnly;
        private boolean copyOnDeath;

        private Builder(Supplier<T> factory) { this.factory = factory; }

        public Builder<T> serialize(Codec<T> codec) { return serialize(codec, value -> true); }

        public Builder<T> serialize(Codec<T> codec, Predicate<T> shouldSave) {
            this.codec = codec;
            this.shouldSave = shouldSave;
            return this;
        }

        public Builder<T> sync(StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
            this.syncCodec = codec;
            return this;
        }

        public Builder<T> syncToOwner(StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
            this.syncCodec = codec;
            this.ownerOnly = true;
            return this;
        }

        public Builder<T> copyOnDeath() { this.copyOnDeath = true; return this; }
    }
}
