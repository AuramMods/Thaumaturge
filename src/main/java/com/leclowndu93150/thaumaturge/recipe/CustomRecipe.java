package com.leclowndu93150.thaumaturge.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

/** Native crafting integration for the baseline's trimmed-input special recipes. */
public abstract class CustomRecipe extends AbstractRecipe<CraftingContainer> implements CraftingRecipe {
    private final CraftingBookCategory category;

    protected CustomRecipe(CraftingBookCategory category) { this.category = category; }

    public abstract boolean matches(CraftingInput input, Level level);
    public abstract ItemStack assemble(CraftingInput input, HolderLookup.Provider registries);

    @Override public final boolean matches(CraftingContainer input, Level level) { return matches(CraftingInput.from(input), level); }
    @Override public final ItemStack assemble(CraftingContainer input, HolderLookup.Provider registries) {
        return assemble(CraftingInput.from(input), registries);
    }
    @Override public boolean isSpecial() { return true; }
    @Override public CraftingBookCategory category() { return category; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }

    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) { return nativeRemainingItems(input); }

    @Override
    public final NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        CraftingInput.Positioned positioned = CraftingInput.ofPositioned(input.getWidth(), input.getHeight(), input.getItems());
        CraftingInput grid = positioned.input();
        NonNullList<ItemStack> remaining = getRemainingItems(grid);
        NonNullList<ItemStack> expanded = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        for (int y = 0; y < grid.height(); y++) {
            for (int x = 0; x < grid.width(); x++) {
                expanded.set(x + positioned.left() + (y + positioned.top()) * input.getWidth(), remaining.get(x + y * grid.width()));
            }
        }
        return expanded;
    }
}
