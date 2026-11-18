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
        if (key.nativeAccess() != null) return isRemoved(stack, key.id().toString()) ? null : key.nativeAccess().get(stack);
        return ItemDataValues.get(values(stack), ItemDataDefaults.get(stack.getItem()), key, ItemDataContext.ops());
    }

    public static <T> T getOrDefault(ItemStack stack, ItemDataKey<T> key, T fallback) {
        T value = get(stack, key);
        return value == null ? fallback : value;
    }

    public static boolean has(ItemStack stack, ItemDataKey<?> key) {
        if (key.nativeAccess() != null) return get(stack, key) != null;
        return !stack.isEmpty() && ItemDataValues.has(values(stack), ItemDataDefaults.get(stack.getItem()), key);
    }

    public static <T> T set(ItemStack stack, ItemDataKey<T> key, T value) {
        if (value == null) return remove(stack, key);
        if (stack.isEmpty()) throw new IllegalArgumentException("Cannot set item data on an empty stack");
        T previous = get(stack, key);
        if (key.nativeAccess() != null) {
            key.encode(ItemDataContext.ops(), value);
            key.nativeAccess().set(stack, value);
            nativeRemoval(stack, key, false);
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
            nativeRemoval(stack, key, key.nativeAccess().defaultValue(stack.getItem()) != null);
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

    /** Reads removal state without initializing schemas during native ItemStack bootstrap. */
    public static boolean isRemoved(ItemStack stack, String id) {
        return ItemDataValues.removed(values(stack), id) && !ItemNbt.nativeFieldPresent(stack.getTag(), id);
    }

    private static void nativeRemoval(ItemStack stack, ItemDataKey<?> key, boolean remove) {
        CompoundTag old = values(stack);
        CompoundTag values = old == null ? new CompoundTag() : old.copy();
        String id = key.id().toString();
        values.remove(id);
        if (remove) values.putBoolean("!" + id, true);
        else values.remove("!" + id);
        store(stack, values);
    }

    public static ItemDataPatch snapshot(ItemStack stack) { return snapshot(stack, ItemData::key); }

    /** Capture only changes from item defaults, retaining removals and registry-scoped values. */
    public static ItemDataPatch snapshot(ItemStack stack, java.util.function.Function<net.minecraft.resources.ResourceLocation, ItemDataKey<?>> lookup) {
        if (stack.isEmpty()) return ItemDataPatch.EMPTY;
        var patch = ItemDataPatch.builder();
        CompoundTag values = values(stack);
        CompoundTag defaults = ItemDataDefaults.get(stack.getItem());
        if (values != null) for (String name : values.getAllKeys().stream().sorted().toList()) {
            boolean removed = name.startsWith("!");
            String id = removed ? name.substring(1) : name;
            var key = lookup.apply(new net.minecraft.resources.ResourceLocation(id));
            if (removed) {
                ItemDataValues.removed(values, id);
                if (key.nativeAccess() == null && defaults.contains(id)) patch.remove(key);
            } else {
                if (key.nativeAccess() != null) throw new IllegalArgumentException("Native item data must use native storage: " + id);
                if (!values.get(name).equals(defaults.get(id))) snapshotValue(patch, key, values.get(name));
            }
        }
        for (var key : VanillaItemData.keys()) snapshotNative(patch, stack, key);
        return patch.build();
    }

    private static <T> void snapshotValue(ItemDataPatch.Builder patch, ItemDataKey<T> key, Tag value) {
        patch.set(key, key.decode(ItemDataContext.ops(), value.copy()));
    }

    private static <T> void snapshotNative(ItemDataPatch.Builder patch, ItemStack stack, ItemDataKey<T> key) {
        T value = get(stack, key);
        T baseline = key.nativeAccess().defaultValue(stack.getItem());
        if (Objects.equals(value, baseline)) return;
        if (value == null) patch.remove(key);
        else patch.set(key, value);
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
        VanillaItemData.canonicalFields(stack, fields);
        if (stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)
                && fields.get("nbt:StoredEnchantments") instanceof net.minecraft.nbt.ListTag list && list.isEmpty()) {
            fields.remove("nbt:StoredEnchantments");
        }
        return java.util.Collections.unmodifiableMap(fields);
    }

    public static boolean listMatches(java.util.List<ItemStack> first, java.util.List<ItemStack> second) {
        if (first.size() != second.size()) return false;
        for (int i = 0; i < first.size(); i++) {
            ItemStack a = first.get(i), b = second.get(i);
            if (a.isEmpty() && b.isEmpty()) continue;
            if (a.getCount() != b.getCount() || a.getItem() != b.getItem()
                    || !comparisonFields(a).equals(comparisonFields(b)) || !a.areCapsCompatible(b)) return false;
        }
        return true;
    }

    /** Like the baseline helper, excludes counts while preserving equality's item/data distinctions. */
    public static int hashStackList(java.util.List<ItemStack> stacks) {
        int hash = 0;
        for (ItemStack stack : stacks) {
            int dataHash = stack.isEmpty() ? 0 : comparisonFields(stack).hashCode();
            hash = 31 * hash + 31 * (31 + stack.getItem().hashCode()) + dataHash;
        }
        return hash;
    }

    public static void copyExplicitData(ItemStack source, ItemStack destination) {
        if (source.getTag() == null) return;
        CompoundTag result = destination.getTag() == null ? new CompoundTag() : destination.getTag().copy();
        CompoundTag explicit = source.getTag().copy();
        VanillaItemData.stripNativeStorage(explicit);
        ItemNbt.copyExplicit(explicit, result);
        ItemStack changed = destination.copy();
        changed.setTag(result.isEmpty() ? null : result);
        var nativePatch = ItemDataPatch.builder();
        for (var key : VanillaItemData.keys()) {
            if (isRemoved(source, key.id().toString())) key.nativeAccess().remove(changed);
            snapshotNative(nativePatch, source, key);
        }
        nativePatch.build().apply(changed);
        destination.setTag(changed.getTag() == null ? null : changed.getTag().copy());
    }
}
