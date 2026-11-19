package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.item.ItemStack;

/** Gives the taint mining bonus the fastest mining rule of a custom tool. */
public interface MiningSpeedProvider {
    float bestMiningSpeed(ItemStack stack);
}
