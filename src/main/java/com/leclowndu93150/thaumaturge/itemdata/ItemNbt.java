package com.leclowndu93150.thaumaturge.itemdata;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Item-field equality and explicit-data copying over the native NBT representation. */
public final class ItemNbt {
    private ItemNbt() {}

    public static Map<String, Tag> fields(CompoundTag tag, CompoundTag defaults) {
        Map<String, Tag> fields = new LinkedHashMap<>();
        for (String name : defaults.getAllKeys()) fields.put("item:" + name, defaults.get(name).copy());
        if (tag == null) return Collections.unmodifiableMap(fields);
        for (String name : tag.getAllKeys()) {
            Tag value = tag.get(name);
            if (name.equals(ItemData.TAG)) {
                if (!(value instanceof CompoundTag values)) throw new IllegalArgumentException("Invalid item data compound");
                for (String key : values.getAllKeys()) {
                    if (key.startsWith("!")) {
                        if (!values.contains(key, Tag.TAG_BYTE) || values.getByte(key) != 1 || values.contains(key.substring(1))) {
                            throw new IllegalArgumentException("Invalid item data removal marker for " + key);
                        }
                        fields.remove("item:" + key.substring(1));
                    } else fields.put("item:" + key, values.get(key).copy());
                }
            } else if (name.equals("display") && value instanceof CompoundTag display) {
                for (String key : display.getAllKeys()) fields.put("display:" + key, display.get(key).copy());
            } else if (!(name.equals("Enchantments") && value instanceof ListTag list && list.isEmpty())) {
                fields.put("nbt:" + name, value.copy());
            }
        }
        return Collections.unmodifiableMap(fields);
    }

    public static boolean matches(Map<String, Tag> reference, Map<String, Tag> candidate, boolean ignoreDamage, boolean subset) {
        for (var entry : reference.entrySet()) {
            if (ignoreDamage && entry.getKey().equals("nbt:Damage")) continue;
            if (!entry.getValue().equals(candidate.get(entry.getKey()))) return false;
        }
        if (subset) return true;
        for (String key : candidate.keySet()) {
            if (ignoreDamage && key.equals("nbt:Damage")) continue;
            if (!reference.containsKey(key)) return false;
        }
        return true;
    }

    /** Replace each explicit field; merge the two NBT containers that hold independent fields. */
    public static void copyExplicit(CompoundTag source, CompoundTag destination) {
        if (source == null) return;
        fields(source, new CompoundTag());
        fields(destination, new CompoundTag());
        for (String name : source.getAllKeys()) {
            Tag value = source.get(name);
            if ((name.equals(ItemData.TAG) || name.equals("display")) && value instanceof CompoundTag fields) {
                if (destination.contains(name) && !(destination.get(name) instanceof CompoundTag)) {
                    throw new IllegalArgumentException("Invalid destination item data container " + name);
                }
                CompoundTag target = destination.getCompound(name).copy();
                for (String key : fields.getAllKeys()) {
                    if (name.equals(ItemData.TAG)) target.remove(key.startsWith("!") ? key.substring(1) : "!" + key);
                    target.put(key, fields.get(key).copy());
                }
                destination.put(name, target);
            } else destination.put(name, value.copy());
        }
    }
}
