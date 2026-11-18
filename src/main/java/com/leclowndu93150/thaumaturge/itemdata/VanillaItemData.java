package com.leclowndu93150.thaumaturge.itemdata;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native 1.20.1 NBT access for vanilla fields referenced by recipe/research patches. */
public final class VanillaItemData {
    public static final ItemDataKey<Component> CUSTOM_NAME = ItemDataKey.nativeKey(id("custom_name"),
            ExtraCodecs.COMPONENT, new ItemDataKey.NativeAccess<>() {
                public Component get(ItemStack stack) { return stack.hasCustomHoverName() ? stack.getHoverName() : null; }
                public void set(ItemStack stack, Component value) { stack.setHoverName(value); }
                public void remove(ItemStack stack) { stack.resetHoverName(); }
            });
    public static final ItemDataKey<Integer> DAMAGE = ItemDataKey.nativeKey(id("damage"),
            Codec.intRange(0, Integer.MAX_VALUE), new ItemDataKey.NativeAccess<>() {
                public Integer get(ItemStack stack) {
                    return stack.getMaxDamage() > 0 || (stack.getTag() != null && stack.getTag().contains("Damage"))
                            ? stack.getDamageValue() : null;
                }
                public void set(ItemStack stack, Integer value) { stack.setDamageValue(value); }
                public void remove(ItemStack stack) { stack.removeTagKey("Damage"); }
                public Integer defaultValue(net.minecraft.world.item.Item item) { return item.getMaxDamage() > 0 ? 0 : null; }
            });
    public static final ItemDataKey<EnchantmentData> ENCHANTMENTS = enchantments("enchantments", "Enchantments", 1, false);
    public static final ItemDataKey<EnchantmentData> STORED_ENCHANTMENTS = enchantments("stored_enchantments", "StoredEnchantments", 32, true);
    public static final ItemDataKey<PotionData> POTION_CONTENTS = ItemDataKey.nativeKey(id("potion_contents"),
            PotionData.CODEC, new ItemDataKey.NativeAccess<>() {
                public PotionData get(ItemStack stack) { return PotionData.readItem(stack); }
                public void set(ItemStack stack, PotionData value) { PotionData.writeItem(stack, value); }
                public void remove(ItemStack stack) { PotionData.removeItem(stack); }
                public PotionData defaultValue(net.minecraft.world.item.Item item) {
                    return item instanceof net.minecraft.world.item.PotionItem || item instanceof net.minecraft.world.item.TippedArrowItem ? PotionData.EMPTY : null;
                }
            });
    private static final Map<ResourceLocation, ItemDataKey<?>> KEYS = Map.of(
            DAMAGE.id(), DAMAGE, CUSTOM_NAME.id(), CUSTOM_NAME,
            ENCHANTMENTS.id(), ENCHANTMENTS, STORED_ENCHANTMENTS.id(), STORED_ENCHANTMENTS,
            POTION_CONTENTS.id(), POTION_CONTENTS);

    private VanillaItemData() {}
    public static java.util.List<ItemDataKey<?>> keys() {
        return java.util.List.of(DAMAGE, CUSTOM_NAME, ENCHANTMENTS, STORED_ENCHANTMENTS, POTION_CONTENTS);
    }
    private static ResourceLocation id(String path) { return ResourceLocation.withDefaultNamespace(path); }

    public static ItemDataKey<?> key(ResourceLocation id) {
        ItemDataKey<?> key = KEYS.get(id);
        if (key == null) throw new IllegalArgumentException("Unknown native item data field " + id);
        return key;
    }

    private static ItemDataKey<EnchantmentData> enchantments(String id, String tagName, int hideFlag, boolean stored) {
        return ItemDataKey.nativeKey(id(id), EnchantmentData.CODEC, new ItemDataKey.NativeAccess<>() {
            public EnchantmentData get(ItemStack stack) {
                CompoundTag tag = stack.getTag();
                if (stored && !stack.is(Items.ENCHANTED_BOOK) && (tag == null || !tag.contains(tagName))) return null;
                return EnchantmentData.readItemTag(tag, tagName, hideFlag);
            }
            public void set(ItemStack stack, EnchantmentData value) {
                CompoundTag tag = stack.getOrCreateTag();
                value.writeItemTag(tag, tagName, hideFlag);
                if (tag.isEmpty()) stack.setTag(null);
            }
            public void remove(ItemStack stack) { set(stack, EnchantmentData.EMPTY); }
            public EnchantmentData defaultValue(net.minecraft.world.item.Item item) {
                return !stored || item == Items.ENCHANTED_BOOK ? EnchantmentData.EMPTY : null;
            }
        });
    }

    static void canonicalFields(ItemStack stack, java.util.Map<String, net.minecraft.nbt.Tag> fields) {
        fields.remove("nbt:Damage");
        fields.remove("display:Name");
        fields.remove("nbt:Enchantments");
        fields.remove("nbt:StoredEnchantments");
        if (fields.get("nbt:HideFlags") instanceof net.minecraft.nbt.NumericTag flags) {
            int remaining = flags.getAsInt() & ~33;
            if (remaining == 0) fields.remove("nbt:HideFlags");
            else fields.put("nbt:HideFlags", net.minecraft.nbt.IntTag.valueOf(remaining));
        }
        PotionData.canonicalFields(stack, fields);
        for (var key : keys()) canonicalField(stack, fields, key);
    }

    private static <T> void canonicalField(ItemStack stack, java.util.Map<String, net.minecraft.nbt.Tag> fields, ItemDataKey<T> key) {
        T value = ItemData.get(stack, key);
        String field = "native:" + key.id();
        if (value == null) fields.remove(field);
        else fields.put(field, key.encode(ItemDataContext.ops(), value));
    }

    /** Remove fields represented by typed native components, retaining unrelated native NBT. */
    static void stripNativeStorage(CompoundTag tag) {
        for (String name : java.util.List.of("Damage", "Enchantments", "StoredEnchantments", "Potion", "CustomPotionColor", "CustomPotionEffects", PotionData.REMOVED_TAG)) tag.remove(name);
        if (tag.contains("display", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag display = tag.getCompound("display").copy();
            display.remove("Name");
            if (display.isEmpty()) tag.remove("display");
            else tag.put("display", display);
        }
        if (tag.contains("HideFlags", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC)) {
            int remaining = tag.getInt("HideFlags") & ~33;
            if (remaining == 0) tag.remove("HideFlags");
            else tag.putInt("HideFlags", remaining);
        }
        if (tag.contains(ItemData.TAG, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag values = tag.getCompound(ItemData.TAG).copy();
            for (var key : keys()) { values.remove(key.id().toString()); values.remove("!" + key.id()); }
            if (values.isEmpty()) tag.remove(ItemData.TAG);
            else tag.put(ItemData.TAG, values);
        }
    }
}
