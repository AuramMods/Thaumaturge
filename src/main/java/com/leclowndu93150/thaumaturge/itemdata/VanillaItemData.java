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
            });
    public static final ItemDataKey<EnchantmentData> ENCHANTMENTS = enchantments("enchantments", "Enchantments", 1, false);
    public static final ItemDataKey<EnchantmentData> STORED_ENCHANTMENTS = enchantments("stored_enchantments", "StoredEnchantments", 32, true);
    public static final ItemDataKey<PotionData> POTION_CONTENTS = ItemDataKey.nativeKey(id("potion_contents"),
            PotionData.CODEC, new ItemDataKey.NativeAccess<>() {
                public PotionData get(ItemStack stack) { return PotionData.readItem(stack); }
                public void set(ItemStack stack, PotionData value) { PotionData.writeItem(stack, value); }
                public void remove(ItemStack stack) { PotionData.removeItem(stack); }
            });
    private static final Map<ResourceLocation, ItemDataKey<?>> KEYS = Map.of(
            DAMAGE.id(), DAMAGE, CUSTOM_NAME.id(), CUSTOM_NAME,
            ENCHANTMENTS.id(), ENCHANTMENTS, STORED_ENCHANTMENTS.id(), STORED_ENCHANTMENTS,
            POTION_CONTENTS.id(), POTION_CONTENTS);

    private VanillaItemData() {}
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
        });
    }
}
