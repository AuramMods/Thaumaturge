package com.leclowndu93150.thaumaturge.datamap;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Owned data-map schema and conflict rules; values belong to individual native registries. */
public final class DataMapType<R, T> {
    private final ResourceKey<Registry<R>> registryKey;
    private final ResourceLocation id;
    private final Codec<T> codec;
    private final Codec<T> networkCodec;
    private final boolean mandatorySync;
    private final DataMapValueMerger<R, T> merger;
    private final Codec<? extends DataMapValueRemover<R, T>> remover;

    private DataMapType(Builder<T, R> builder) {
        registryKey = builder.registryKey; id = builder.id; codec = builder.codec;
        networkCodec = builder.networkCodec; mandatorySync = builder.mandatorySync;
        merger = builder.merger; remover = builder.remover;
    }

    public ResourceKey<Registry<R>> registryKey() { return registryKey; }
    public ResourceLocation id() { return id; }
    public Codec<T> codec() { return codec; }
    public Codec<T> networkCodec() { return networkCodec; }
    public boolean mandatorySync() { return mandatorySync; }
    public DataMapValueMerger<R, T> merger() { return merger; }
    public Codec<? extends DataMapValueRemover<R, T>> remover() { return remover; }

    public static <T, R> Builder<T, R> builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return new Builder<>(id, registry, codec);
    }

    public static final class Builder<T, R> {
        private final ResourceKey<Registry<R>> registryKey;
        private final ResourceLocation id;
        private final Codec<T> codec;
        private Codec<T> networkCodec;
        private boolean mandatorySync;
        private DataMapValueMerger<R, T> merger = (registry, first, oldValue, second, newValue) -> newValue;
        private Codec<? extends DataMapValueRemover<R, T>> remover;

        private Builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
            this.id = Objects.requireNonNull(id); this.registryKey = Objects.requireNonNull(registry);
            this.codec = Objects.requireNonNull(codec);
        }
        public Builder<T, R> synced(Codec<T> networkCodec, boolean mandatory) {
            this.networkCodec = Objects.requireNonNull(networkCodec); mandatorySync = mandatory; return this;
        }
        public Builder<T, R> merger(DataMapValueMerger<R, T> merger) { this.merger = Objects.requireNonNull(merger); return this; }
        public Builder<T, R> remover(Codec<? extends DataMapValueRemover<R, T>> remover) { this.remover = Objects.requireNonNull(remover); return this; }
        public DataMapType<R, T> build() { return new DataMapType<>(this); }
    }
}
