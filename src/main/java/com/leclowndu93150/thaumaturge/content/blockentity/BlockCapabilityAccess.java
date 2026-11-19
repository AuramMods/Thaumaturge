package com.leclowndu93150.thaumaturge.content.blockentity;

import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jspecify.annotations.Nullable;

public final class BlockCapabilityAccess {
    private BlockCapabilityAccess() {}

    public static <T> @Nullable T find(
            Level level, BlockPos pos, Capability<T> capability, @Nullable Direction side) {
        if (!LoadedChunks.contains(level, pos)) {
            return null;
        }
        var entity = level.getBlockEntity(pos);
        return entity == null || entity.isRemoved() ? null : entity.getCapability(capability, side).orElse(null);
    }

    public static @Nullable IItemHandler items(Level level, BlockPos pos, @Nullable Direction side) {
        if (!LoadedChunks.contains(level, pos)) {
            return null;
        }
        var entity = level.getBlockEntity(pos);
        if (entity != null) {
            return entity.isRemoved() ? null : entity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        }
        // The vanilla composter has a sided container but no block entity to expose it through Forge.
        return level.getBlockState(pos).is(Blocks.COMPOSTER) ? new ComposterHandler(level, pos.immutable(), side) : null;
    }

    private record ComposterHandler(Level level, BlockPos pos, @Nullable Direction side) implements IItemHandler {
        private IItemHandler current() {
            if (!LoadedChunks.contains(level, pos)) {
                return EmptyHandler.INSTANCE;
            }
            var state = level.getBlockState(pos);
            if (!state.is(Blocks.COMPOSTER)) {
                return EmptyHandler.INSTANCE;
            }
            var container = ((WorldlyContainerHolder) state.getBlock()).getContainer(state, level, pos);
            return new SidedInvWrapper(container, side);
        }

        @Override public int getSlots() { return current().getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return current().getStackInSlot(slot); }
        @Override public int getSlotLimit(int slot) { return current().getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return current().isItemValid(slot, stack); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return current().insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return current().extractItem(slot, amount, simulate);
        }
    }
}
