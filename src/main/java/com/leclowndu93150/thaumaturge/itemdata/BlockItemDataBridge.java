package com.leclowndu93150.thaumaturge.itemdata;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/** Native placement, loot and pick/save-to-item entry points. */
public final class BlockItemDataBridge {
    public static final String PRESERVED_DATA = "thaumaturge:item_data";
    private BlockItemDataBridge() {}

    public static void applyPlacement(Level level, BlockPos pos, ItemStack stack) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BlockItemData data) {
            ItemDataContext.withRegistries(level.registryAccess(), () -> {
                Set<ResourceLocation> consumed = new HashSet<>();
                data.applyItemData(ItemDataReader.fromStack(stack, key -> consumed.add(key.id())));
                var preserved = BlockItemNbt.capture(stack.getTag(), consumed);
                if (preserved.isEmpty()) blockEntity.getPersistentData().remove(PRESERVED_DATA);
                else blockEntity.getPersistentData().put(PRESERVED_DATA, preserved);
                blockEntity.setChanged();
                return null;
            });
        }
    }

    public static void saveToItem(BlockEntity blockEntity, ItemStack stack) {
        if (!(blockEntity instanceof BlockItemData data)) return;
        copyToItem(blockEntity, stack, ItemDataSelection.ALL);
        var tag = BlockItem.getBlockEntityData(stack);
        if (tag != null) {
            data.removeItemDataFromTag(tag);
            var forgeData = tag.getCompound("ForgeData");
            forgeData.remove(PRESERVED_DATA);
            if (forgeData.isEmpty()) tag.remove("ForgeData");
        }
    }

    public static void copyToItem(BlockEntity blockEntity, ItemStack stack, ItemDataSelection selection) {
        if (!(blockEntity instanceof BlockItemData data)) return;
        Runnable copy = () -> {
            ItemStack changed = stack.copy();
            var preserved = BlockItemNbt.select(blockEntity.getPersistentData().getCompound(PRESERVED_DATA), selection);
            if (!preserved.isEmpty()) ItemNbt.copyExplicit(preserved, changed.getOrCreateTag());
            data.collectItemData().select(selection).apply(changed);
            stack.setTag(changed.getTag() == null ? null : changed.getTag().copy());
        };
        Level level = blockEntity.getLevel();
        if (level == null) copy.run();
        else ItemDataContext.withRegistries(level.registryAccess(), () -> { copy.run(); return null; });
    }
}
