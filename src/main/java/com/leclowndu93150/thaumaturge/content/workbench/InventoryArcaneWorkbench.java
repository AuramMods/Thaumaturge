package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.api.transaction.SnapshotJournal;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class InventoryArcaneWorkbench extends SimpleContainer implements IArcaneWorkbench, CraftingContainer {
    public static final int CRAFTING_SLOTS = 9;
    public static final int CRYSTAL_SLOTS = 6;
    public static final int WAND_SLOT = CRAFTING_SLOTS + CRYSTAL_SLOTS;
    public static final int SIZE = WAND_SLOT + 1;

    private final SnapshotJournal<List<ItemStack>> craftJournal = new SnapshotJournal<>() {
        @Override
        protected List<ItemStack> createSnapshot() {
            return getItems().stream().map(ItemStack::copy).toList();
        }

        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) {
            replaceItems(snapshot);
        }

        @Override
        protected void onRootCommit(List<ItemStack> original) {
            publishCraftChange();
        }
    };

    private final List<Runnable> changeListeners = new ArrayList<>();

    public InventoryArcaneWorkbench() {
        super(SIZE);
    }

    @Override
    public ItemStack wandStack() {
        return getItem(WAND_SLOT);
    }

    @Override
    public AspectList availableCrystals() {
        AspectList crystals = AspectList.EMPTY;
        for (int slot = CRAFTING_SLOTS; slot < WAND_SLOT; slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemEssentiaCrystal)) {
                continue;
            }
            Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(stack);
            if (aspect != null) {
                crystals = crystals.add(aspect, stack.getCount());
            }
        }
        return crystals;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == WAND_SLOT) {
            return SlotWorkbenchWand.isUsableWand(stack);
        }
        return super.canPlaceItem(slot, stack);
    }

    public void addChangedListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangedListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public CraftingContainer asVanillaCraftingInput(AbstractContainerMenu menu) {
        // Forge recipes can address the full grid, including empty slots, by index.
        var grid = NonNullList.withSize(CRAFTING_SLOTS, ItemStack.EMPTY);
        for (int slot = 0; slot < CRAFTING_SLOTS; slot++) {
            grid.set(slot, getItem(slot));
        }
        return new TransientCraftingContainer(menu, getWidth(), getHeight(), grid);
    }

    void applyCraft(List<ItemStack> items, TransactionContext transaction) {
        craftJournal.updateSnapshots(transaction);
        replaceItems(items);
    }

    private void replaceItems(List<ItemStack> items) {
        for (int i = 0; i < SIZE; i++) {
            getItems().set(i, items.get(i).copy());
        }
    }

    private void publishCraftChange() {
        setChanged();
    }

    public ArcaneCraftingInput asArcaneCraftInput() {
        return ArcaneCraftingInput.of(3, 3, getItems());
    }

    public ArcaneCraftingInput.Positioned asPositionedArcaneCraftInput() {
        return ArcaneCraftingInput.ofPositioned(3, 3, getItems());
    }
}
