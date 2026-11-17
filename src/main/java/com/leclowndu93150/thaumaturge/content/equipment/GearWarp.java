package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.itemdata.ItemDataDefaults;

import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.world.item.Item;

public final class GearWarp {
    private GearWarp() {}

    public static Item.Properties with(Item.Properties properties, int warp) {
        return ItemDataDefaults.with(properties, TTDataComponents.STACK_WARP.get(), warp);
    }
}
