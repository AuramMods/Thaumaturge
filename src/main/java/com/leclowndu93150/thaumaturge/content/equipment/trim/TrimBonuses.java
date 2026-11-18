package com.leclowndu93150.thaumaturge.content.equipment.trim;

import com.leclowndu93150.thaumaturge.datamap.DataMaps;

import com.leclowndu93150.thaumaturge.api.items.trim.TrimMaterialBonus;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import net.minecraft.world.item.ItemStack;

public final class TrimBonuses {
    private static final TrimMaterialBonus NONE = new TrimMaterialBonus(0, 0);

    private TrimBonuses() {}

    public static TrimMaterialBonus of(ItemStack stack) {
        var trim = ItemData.get(stack, VanillaItemData.TRIM);
        if (trim == null) {
            return NONE;
        }
        TrimMaterialBonus bonus = DataMaps.get(trim.material(), TrimMaterialBonus.DATA_MAP);
        return bonus == null ? NONE : bonus;
    }
}
