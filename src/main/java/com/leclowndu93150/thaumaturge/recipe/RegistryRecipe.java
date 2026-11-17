package com.leclowndu93150.thaumaturge.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/** Keeps existing registry-aware implementations behind native 1.20.1 callback signatures. */
public interface RegistryRecipe<C extends Container> extends Recipe<C>, IdentifiedRecipe {
    ItemStack assemble(C input, HolderLookup.Provider registries);
    ItemStack getResultItem(HolderLookup.Provider registries);

    @Override
    default ItemStack assemble(C input, RegistryAccess registries) {
        return assemble(input, (HolderLookup.Provider) registries);
    }

    @Override
    default ItemStack getResultItem(RegistryAccess registries) {
        return getResultItem((HolderLookup.Provider) registries);
    }
}
