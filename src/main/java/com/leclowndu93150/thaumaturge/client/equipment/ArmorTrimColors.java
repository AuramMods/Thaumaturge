package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.compat.Colors;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import net.minecraft.core.Holder;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.TrimMaterial;

public final class ArmorTrimColors {
    private static final int UNTINTED = -1;

    private ArmorTrimColors() {}

    public static int of(Holder<TrimMaterial> material) {
        TrimMaterial value = material.value();
        if (!value.assetName().equals(TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET)) {
            return UNTINTED;
        }
        TextColor color = value.description().getStyle().getColor();
        return color == null ? UNTINTED : Colors.ARGB32.opaque(color.getValue());
    }

    public static int of(ItemStack stack) {
        var trim = ItemData.get(stack, VanillaItemData.TRIM);
        return trim == null ? UNTINTED : of(trim.material());
    }
}
