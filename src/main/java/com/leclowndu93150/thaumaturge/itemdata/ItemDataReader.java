package com.leclowndu93150.thaumaturge.itemdata;

import net.minecraft.world.item.ItemStack;

/** Read-only typed input for block placement and state-transfer tests. */
public interface ItemDataReader {
    <T> T get(ItemDataKey<T> key);

    static ItemDataReader fromStack(ItemStack stack) {
        return fromStack(stack, key -> {});
    }

    static ItemDataReader fromStack(ItemStack stack, java.util.function.Consumer<ItemDataKey<?>> consumed) {
        return new ItemDataReader() {
            @Override public <T> T get(ItemDataKey<T> key) {
                consumed.accept(key);
                return ItemData.get(stack, key);
            }
        };
    }

    static ItemDataReader fromPatch(ItemDataPatch patch) {
        return new ItemDataReader() {
            @Override public <T> T get(ItemDataKey<T> key) { return patch.get(key); }
        };
    }
}
