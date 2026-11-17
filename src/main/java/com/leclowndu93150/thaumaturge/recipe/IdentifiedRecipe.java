package com.leclowndu93150.thaumaturge.recipe;

import net.minecraft.resources.ResourceLocation;

public interface IdentifiedRecipe {
    void bindRecipeId(ResourceLocation id);
    ResourceLocation getId();
}
