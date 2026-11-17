package com.leclowndu93150.thaumaturge.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A recipe's input view, exposed to 1.20.1 recipes through Container's read operations. */
public interface RecipeInput extends Container {
    int size();

    @Override default int getContainerSize() { return size(); }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) if (!getItem(i).isEmpty()) return false;
        return true;
    }

    @Override default ItemStack removeItem(int slot, int count) { throw readOnly(); }
    @Override default ItemStack removeItemNoUpdate(int slot) { throw readOnly(); }
    @Override default void setItem(int slot, ItemStack stack) { throw readOnly(); }
    @Override default void setChanged() { throw readOnly(); }
    @Override default void clearContent() { throw readOnly(); }
    @Override default boolean stillValid(Player player) { throw readOnly(); }

    private static UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("Recipe inputs are views, not inventory containers");
    }
}
