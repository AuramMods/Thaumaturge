package com.leclowndu93150.thaumaturge.recipe;

import net.minecraft.resources.ResourceLocation;

/** Adds resource-ID binding; reads use the native Recipe#getId contract. */
public interface IdentifiedRecipe {
    void bindRecipeId(ResourceLocation id);
}
