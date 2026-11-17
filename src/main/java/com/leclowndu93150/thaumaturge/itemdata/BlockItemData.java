package com.leclowndu93150.thaumaturge.itemdata;

import net.minecraft.nbt.CompoundTag;

/** Explicit block/item state transfer on Forge 1.20.1. World persistence is independent. */
public interface BlockItemData {
    void collectItemData(ItemDataPatch.Builder output);
    void applyItemData(ItemDataReader input);

    /** Remove fields exported separately when native saveToItem writes BlockEntityTag. */
    default void removeItemDataFromTag(CompoundTag output) {}

    default ItemDataPatch collectItemData() {
        ItemDataPatch.Builder output = ItemDataPatch.builder();
        collectItemData(output);
        return output.build();
    }
}
