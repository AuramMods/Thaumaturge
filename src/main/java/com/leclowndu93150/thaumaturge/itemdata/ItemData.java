package com.leclowndu93150.thaumaturge.itemdata;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** Typed item data persisted, copied, compared and synchronized by native ItemStack NBT. */
public final class ItemData {
    public static final String TAG = "thaumaturge:components";
    private static ItemDataRegistry catalog;

    private ItemData() {}

    public static void registerCatalog(ItemDataRegistry registry) {
        if (catalog != null) throw new IllegalStateException("Item data catalog already registered");
        catalog = Objects.requireNonNull(registry);
    }

    public static ItemDataKey<?> key(net.minecraft.resources.ResourceLocation id) {
        if (id.getNamespace().equals("minecraft")) return VanillaItemData.key(id);
        return Objects.requireNonNull(catalog, "Item data catalog has not been registered").get(id);
    }

    public static <T> T get(ItemStack stack, ItemDataKey<T> key) {
        if (stack.isEmpty()) return null;
        if (key.nativeAccess() != null) return key.nativeAccess().get(stack);
        return ItemDataValues.get(values(stack), ItemDataDefaults.get(stack.getItem()), key, ItemDataContext.ops());
    }

    public static <T> T getOrDefault(ItemStack stack, ItemDataKey<T> key, T fallback) {
        T value = get(stack, key);
        return value == null ? fallback : value;
    }

    public static boolean has(ItemStack stack, ItemDataKey<?> key) {
        if (key.nativeAccess() != null) return !stack.isEmpty() && key.nativeAccess().get(stack) != null;
        return !stack.isEmpty() && ItemDataValues.has(values(stack), ItemDataDefaults.get(stack.getItem()), key);
    }

    public static <T> T set(ItemStack stack, ItemDataKey<T> key, T value) {
        if (value == null) return remove(stack, key);
        if (stack.isEmpty()) throw new IllegalArgumentException("Cannot set item data on an empty stack");
        T previous = get(stack, key);
        if (key.nativeAccess() != null) {
            key.encode(ItemDataContext.ops(), value);
            key.nativeAccess().set(stack, value);
            return previous;
        }
        CompoundTag values = values(stack);
        values = values == null ? new CompoundTag() : values.copy();
        ItemDataValues.set(values, ItemDataDefaults.get(stack.getItem()), key, value, ItemDataContext.ops());
        store(stack, values);
        return previous;
    }

    public static <T> T remove(ItemStack stack, ItemDataKey<T> key) {
        if (stack.isEmpty()) return null;
        T previous = get(stack, key);
        if (key.nativeAccess() != null) {
            key.nativeAccess().remove(stack);
            return previous;
        }
        CompoundTag values = values(stack);
        values = values == null ? new CompoundTag() : values.copy();
        ItemDataValues.remove(values, ItemDataDefaults.get(stack.getItem()), key);
        store(stack, values);
        return previous;
    }

    private static CompoundTag values(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG)) return null;
        if (!tag.contains(TAG, Tag.TAG_COMPOUND)) throw new IllegalArgumentException("Invalid item data compound");
        return tag.getCompound(TAG);
    }

    private static void store(ItemStack stack, CompoundTag values) {
        if (!values.isEmpty()) stack.getOrCreateTag().put(TAG, values);
        else if (stack.getTag() != null) {
            stack.getTag().remove(TAG);
            if (stack.getTag().isEmpty()) stack.setTag(null);
        }
    }

    public static ItemStack create(net.minecraft.world.level.ItemLike item, int count, ItemDataPatch patch) {
        ItemStack stack = new ItemStack(item, count);
        patch.apply(stack);
        return stack;
    }

    public static ItemStack create(net.minecraft.core.Holder<net.minecraft.world.item.Item> item, int count, ItemDataPatch patch) {
        return create(item.value(), count, patch);
    }

    public static java.util.Map<String, Tag> comparisonFields(ItemStack stack) {
        var fields = new java.util.LinkedHashMap<>(ItemNbt.fields(stack.getTag(), ItemDataDefaults.get(stack.getItem())));
        if (stack.getMaxDamage() > 0) fields.putIfAbsent("nbt:Damage", net.minecraft.nbt.IntTag.valueOf(stack.getDamageValue()));
        if (stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)
                && fields.get("nbt:StoredEnchantments") instanceof net.minecraft.nbt.ListTag list && list.isEmpty()) {
            fields.remove("nbt:StoredEnchantments");
        }
        return java.util.Collections.unmodifiableMap(fields);
    }

    public static void copyExplicitData(ItemStack source, ItemStack destination) {
        if (source.getTag() == null) return;
        CompoundTag result = destination.getTag() == null ? new CompoundTag() : destination.getTag().copy();
        ItemNbt.copyExplicit(source.getTag(), result);
        destination.setTag(result.isEmpty() ? null : result);
    }
}
