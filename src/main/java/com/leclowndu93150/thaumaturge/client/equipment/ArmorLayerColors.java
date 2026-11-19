package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.compat.Colors.ARGB32;
import com.leclowndu93150.thaumaturge.content.equipment.ArmorMaterialData;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public final class ArmorLayerColors {
    public static final int ROBE_COLOR = 0xFF6A3880;

    private ArmorLayerColors() {}

    public static int wornTint(ItemStack stack, ArmorMaterialData material, ArmorMaterialData.Layer layer) {
        if (!layer.dyeable()) {
            return -1;
        }
        // The source equipped robe extensions supply this tint for every stack.
        if (material == TTMaterials.ARMOR_ROBES || material == TTMaterials.ARMOR_VOID_ROBE) {
            return ROBE_COLOR;
        }
        return dyeColor(stack, 0);
    }

    public static int dyeColor(ItemStack stack, int fallback) {
        CompoundTag display = stack.getTagElement(DyeableLeatherItem.TAG_DISPLAY);
        return display != null && display.contains(DyeableLeatherItem.TAG_COLOR, Tag.TAG_ANY_NUMERIC)
                ? ARGB32.opaque(display.getInt(DyeableLeatherItem.TAG_COLOR))
                : fallback;
    }
}
