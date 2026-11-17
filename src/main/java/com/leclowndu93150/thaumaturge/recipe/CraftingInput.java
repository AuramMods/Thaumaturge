package com.leclowndu93150.thaumaturge.recipe;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

/** Baseline grid trimming and coordinates with a native crafting-container read view. */
public final class CraftingInput implements RecipeInput, CraftingContainer {
    public static final CraftingInput EMPTY = new CraftingInput(0, 0, List.of());
    private final int width;
    private final int height;
    private final List<ItemStack> items;
    private final StackedContents stackedContents = new StackedContents();
    private final int ingredientCount;

    private CraftingInput(int width, int height, List<ItemStack> items) {
        this.width = width;
        this.height = height;
        this.items = items;
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                count++;
                stackedContents.accountStack(stack, 1);
            }
        }
        ingredientCount = count;
    }

    public static CraftingInput of(int width, int height, List<ItemStack> items) {
        return ofPositioned(width, height, items).input();
    }

    public static Positioned ofPositioned(int width, int height, List<ItemStack> items) {
        if (width == 0 || height == 0) return Positioned.EMPTY;
        int left = width - 1, right = 0, top = height - 1, bottom = 0;
        for (int y = 0; y < height; y++) {
            boolean empty = true;
            for (int x = 0; x < width; x++) {
                if (!items.get(x + y * width).isEmpty()) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    empty = false;
                }
            }
            if (!empty) {
                top = Math.min(top, y);
                bottom = Math.max(bottom, y);
            }
        }
        int trimmedWidth = right - left + 1, trimmedHeight = bottom - top + 1;
        if (trimmedWidth <= 0 || trimmedHeight <= 0) return Positioned.EMPTY;
        if (trimmedWidth == width && trimmedHeight == height) {
            return new Positioned(new CraftingInput(width, height, items), left, top);
        }
        List<ItemStack> trimmed = new ArrayList<>(trimmedWidth * trimmedHeight);
        for (int y = 0; y < trimmedHeight; y++) {
            for (int x = 0; x < trimmedWidth; x++) trimmed.add(items.get(x + left + (y + top) * width));
        }
        return new Positioned(new CraftingInput(trimmedWidth, trimmedHeight, trimmed), left, top);
    }

    public static CraftingInput from(CraftingContainer input) {
        return input instanceof CraftingInput crafting ? crafting : of(input.getWidth(), input.getHeight(), input.getItems());
    }

    @Override public ItemStack getItem(int index) { return items.get(index); }
    public ItemStack getItem(int x, int y) { return getItem(x + y * width); }
    @Override public int size() { return items.size(); }
    @Override public boolean isEmpty() { return ingredientCount == 0; }
    public int width() { return width; }
    public int height() { return height; }
    public List<ItemStack> items() { return items; }
    public int ingredientCount() { return ingredientCount; }
    public StackedContents stackedContents() { return stackedContents; }
    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override public List<ItemStack> getItems() { return items; }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (ItemStack item : items) contents.accountSimpleStack(item);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof CraftingInput input && width == input.width && height == input.height
                && ingredientCount == input.ingredientCount && ItemData.listMatches(items, input.items);
    }

    @Override public int hashCode() { return 31 * (31 * ItemData.hashStackList(items) + width) + height; }

    public record Positioned(CraftingInput input, int left, int top) {
        public static final Positioned EMPTY = new Positioned(CraftingInput.EMPTY, 0, 0);
    }
}
