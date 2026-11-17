package com.leclowndu93150.thaumaturge.serialization;

import net.minecraft.resources.RegistryOps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jspecify.annotations.Nullable;

public final class TTNbt {
    private static final Logger LOGGER = LoggerFactory.getLogger("thaumaturge");

    private TTNbt() {}

    public static <T> Optional<T> read(CompoundTag tag, String key, Codec<T> codec, HolderLookup.Provider registries) {
        return read(tag, key, codec, RegistryOps.create(NbtOps.INSTANCE, registries));
    }

    public static <T> Optional<T> read(CompoundTag tag, String key, Codec<T> codec) {
        return read(tag, key, codec, NbtOps.INSTANCE);
    }

    private static <T> Optional<T> read(CompoundTag tag, String key, Codec<T> codec, DynamicOps<Tag> ops) {
        Tag value = tag.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return codec.parse(ops, value)
                .resultOrPartial(error -> LOGGER.error("Failed to decode '{}': {}", key, error));
    }

    public static <T> void store(
            CompoundTag tag, String key, Codec<T> codec, HolderLookup.Provider registries, T value) {
        store(tag, key, codec, RegistryOps.create(NbtOps.INSTANCE, registries), value);
    }

    public static <T> void store(CompoundTag tag, String key, Codec<T> codec, T value) {
        store(tag, key, codec, NbtOps.INSTANCE, value);
    }

    private static <T> void store(CompoundTag tag, String key, Codec<T> codec, DynamicOps<Tag> ops, T value) {
        codec.encodeStart(ops, value)
                .resultOrPartial(error -> LOGGER.error("Failed to encode '{}': {}", key, error))
                .ifPresent(encoded -> tag.put(key, encoded));
    }

    public static <T> void storeNullable(
            CompoundTag tag, String key, Codec<T> codec, HolderLookup.Provider registries, @Nullable T value) {
        if (value != null) {
            store(tag, key, codec, registries, value);
        }
    }

    public static <T> void storeNullable(CompoundTag tag, String key, Codec<T> codec, @Nullable T value) {
        if (value != null) {
            store(tag, key, codec, value);
        }
    }
}
