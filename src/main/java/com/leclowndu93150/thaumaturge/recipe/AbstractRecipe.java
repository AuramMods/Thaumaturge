package com.leclowndu93150.thaumaturge.recipe;

import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Recipe identity is supplied by its real resource/network ID, never by a placeholder. */
public abstract class AbstractRecipe<C extends Container> implements RegistryRecipe<C> {
    private volatile ResourceLocation recipeId;
    private volatile HolderLookup.Provider registryLookup;

    @Override
    public final synchronized void bindRecipeId(ResourceLocation id) {
        checkId(id);
        recipeId = id;
    }

    public final synchronized void bindRecipeId(ResourceLocation id, HolderLookup.Provider registries) {
        checkId(id);
        registryLookup = Objects.requireNonNull(registries);
        recipeId = id;
    }

    public final HolderLookup.Provider recipeRegistryLookup() { return registryLookup; }

    private void checkId(ResourceLocation id) {
        Objects.requireNonNull(id);
        if (recipeId != null && !recipeId.equals(id)) {
            throw new IllegalStateException("Recipe " + recipeId + " cannot be rebound to " + id);
        }
    }

    @Override
    public final ResourceLocation getId() {
        if (recipeId == null) throw new IllegalStateException("Recipe has not been assigned a resource ID");
        return recipeId;
    }

    protected final NonNullList<ItemStack> nativeRemainingItems(C input) {
        return RegistryRecipe.super.getRemainingItems(input);
    }
}
