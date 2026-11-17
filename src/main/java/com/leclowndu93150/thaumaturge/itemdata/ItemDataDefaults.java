package com.leclowndu93150.thaumaturge.itemdata;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/** Immutable encoded defaults transferred from properties by the native Item constructor hook. */
public final class ItemDataDefaults {
    private static final Map<Item.Properties, CompoundTag> PROPERTIES = new WeakHashMap<>();
    private static final Map<Item, CompoundTag> ITEMS = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final CompoundTag EMPTY = new CompoundTag();

    private ItemDataDefaults() {}

    public static synchronized <T> Item.Properties with(Item.Properties properties, ItemDataKey<T> key, T value) {
        var encoded = key.encode(ItemDataContext.ops(), value);
        PROPERTIES.computeIfAbsent(properties, unused -> new CompoundTag()).put(key.id().toString(), encoded.copy());
        return properties;
    }

    public static synchronized void bind(Item item, Item.Properties properties) {
        CompoundTag defaults = PROPERTIES.get(properties);
        if (defaults != null) ITEMS.put(item, defaults.copy());
    }

    static CompoundTag get(Item item) { return ITEMS.getOrDefault(item, EMPTY); }
}
