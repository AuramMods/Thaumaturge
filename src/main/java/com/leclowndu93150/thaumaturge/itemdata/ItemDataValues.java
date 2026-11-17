package com.leclowndu93150.thaumaturge.itemdata;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/** NBT value operations, shared by stacks and item-default construction. */
public final class ItemDataValues {
    private ItemDataValues() {}

    public static <T> T get(CompoundTag values, CompoundTag defaults, ItemDataKey<T> key, DynamicOps<Tag> ops) {
        String name = key.id().toString();
        if (removed(values, name)) return null;
        Tag stored = values == null ? null : values.get(name);
        if (stored == null) stored = defaults.get(name);
        return stored == null ? null : key.decode(ops, stored.copy());
    }

    public static boolean has(CompoundTag values, CompoundTag defaults, ItemDataKey<?> key) {
        String name = key.id().toString();
        return !removed(values, name)
                && ((values != null && values.contains(name)) || defaults.contains(name));
    }

    public static <T> void set(CompoundTag values, CompoundTag defaults, ItemDataKey<T> key,
            T value, DynamicOps<Tag> ops) {
        // Validate before changing either the old value or its removal marker.
        Tag encoded = key.encode(ops, value);
        String name = key.id().toString();
        values.remove("!" + name);
        if (encoded.equals(defaults.get(name))) values.remove(name);
        else values.put(name, encoded.copy());
    }

    public static void remove(CompoundTag values, CompoundTag defaults, ItemDataKey<?> key) {
        String name = key.id().toString();
        values.remove(name);
        if (defaults.contains(name)) values.putBoolean("!" + name, true);
        else values.remove("!" + name);
    }

    private static boolean removed(CompoundTag values, String name) {
        if (values == null || !values.contains("!" + name)) return false;
        if (!values.contains("!" + name, Tag.TAG_BYTE) || values.getByte("!" + name) != 1 || values.contains(name)) {
            throw new IllegalArgumentException("Invalid item data removal marker for " + name);
        }
        return true;
    }
}
