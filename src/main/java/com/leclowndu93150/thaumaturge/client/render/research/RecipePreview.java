package com.leclowndu93150.thaumaturge.client.render.research;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.jspecify.annotations.Nullable;

final class RecipePreview {
    private RecipePreview() {}

    static ItemStack smithingResult(SmithingRecipe recipe, @Nullable RegistryAccess registries,
            ItemStack template, ItemStack base, ItemStack addition) {
        if (registries == null || template.isEmpty() || base.isEmpty() || addition.isEmpty()) {
            return resultOf(recipe, registries);
        }
        var inputs = new SimpleContainer(template.copy(), base.copy(), addition.copy());
        ItemStack assembled = recipe.assemble(inputs, registries);
        return assembled.isEmpty() ? resultOf(recipe, registries) : assembled;
    }

    static ItemStack resultOf(Recipe<?> recipe, @Nullable RegistryAccess registries) {
        if (registries == null) {
            return ItemStack.EMPTY;
        }
        try {
            ItemStack result = recipe.getResultItem(registries);
            return result == null ? ItemStack.EMPTY : result;
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }
}
