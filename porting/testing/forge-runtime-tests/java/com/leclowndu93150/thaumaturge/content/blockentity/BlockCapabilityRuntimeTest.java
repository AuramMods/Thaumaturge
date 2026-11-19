package com.leclowndu93150.thaumaturge.content.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

class BlockCapabilityRuntimeTest {
    @Test
    void realForgeBlockEntityClosesSidedViewsWhenRotatedUnloadedAndRemoved() {
        var pos = new BlockPos(18, 64, -31);
        var north = Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        var machine = new Machine(pos, north);
        var level = mock(Level.class);
        when(level.hasChunk(1, -2)).thenReturn(true);
        when(level.getBlockEntity(pos)).thenReturn(machine);
        var items = machine.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH);
        assertSame(items, machine.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH));
        assertSame(machine.items, BlockCapabilityAccess.items(level, pos, Direction.NORTH));
        assertNull(BlockCapabilityAccess.items(level, pos, Direction.SOUTH));
        assertNull(BlockCapabilityAccess.items(level, pos, null));
        assertSame(machine.tank, BlockCapabilityAccess.find(level, pos, ForgeCapabilities.FLUID_HANDLER, null));
        assertSame(machine.energy, BlockCapabilityAccess.find(level, pos, ForgeCapabilities.ENERGY, Direction.NORTH));
        assertNull(BlockCapabilityAccess.find(level, pos, ForgeCapabilities.ENERGY, Direction.SOUTH));

        var invalidations = new AtomicInteger();
        items.addListener(closed -> invalidations.incrementAndGet());
        machine.setBlockState(north);
        assertTrue(items.isPresent());
        machine.setBlockState(north.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
        assertFalse(items.isPresent());
        assertEquals(1, invalidations.get());
        assertNull(BlockCapabilityAccess.items(level, pos, Direction.NORTH));
        var south = machine.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.SOUTH);
        assertSame(machine.items, south.orElse(null));

        machine.onChunkUnloaded();
        assertFalse(south.isPresent());
        assertNull(BlockCapabilityAccess.items(level, pos, Direction.SOUTH));
        assertNull(BlockCapabilityAccess.find(level, pos, ForgeCapabilities.FLUID_HANDLER, null));
        machine.reviveCaps();
        var restored = machine.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.SOUTH);
        assertNotSame(south, restored);
        assertSame(machine.items, restored.orElse(null));
        machine.setRemoved();
        assertFalse(restored.isPresent());
        assertNull(BlockCapabilityAccess.items(level, pos, Direction.SOUTH));
        // The native chunk calls clearRemoved when a block entity is placed back.
        machine.clearRemoved();
        assertSame(machine.items, BlockCapabilityAccess.items(level, pos, Direction.SOUTH));
    }

    @Test
    void nativeFurnaceExposesDifferentTopAndBottomSlotsWithoutLoadingAnAbsentChunk() {
        var level = mock(Level.class);
        var pos = new BlockPos(34, 64, 19);
        var furnace = new FurnaceBlockEntity(pos, Blocks.FURNACE.defaultBlockState());
        when(level.getBlockEntity(pos)).thenReturn(furnace);
        assertNull(BlockCapabilityAccess.items(level, pos, Direction.UP));
        assertNull(BlockCapabilityAccess.find(level, pos, ForgeCapabilities.FLUID_HANDLER, Direction.UP));
        verify(level, never()).getBlockEntity(pos);
        verify(level, never()).getBlockState(pos);
        when(level.hasChunk(2, 1)).thenReturn(true);
        assertEquals(1, BlockCapabilityAccess.items(level, pos, Direction.UP).getSlots());
        assertEquals(2, BlockCapabilityAccess.items(level, pos, Direction.DOWN).getSlots());
        assertEquals(3, BlockCapabilityAccess.items(level, pos, null).getSlots());
    }

    @Test
    void composterLookupFollowsTheLiveStateAndStopsAtReplacementOrUnload() {
        var level = mock(Level.class);
        var pos = new BlockPos(5, 64, 4);
        var empty = Blocks.COMPOSTER.defaultBlockState();
        var state = new AtomicReference<>(empty);
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(level.getBlockState(pos)).thenAnswer(call -> state.get());
        var input = BlockCapabilityAccess.items(level, pos, Direction.UP);
        var output = BlockCapabilityAccess.items(level, pos, Direction.DOWN);
        assertEquals(1, input.getSlots());
        assertEquals(0, output.getSlots());
        state.set(empty.setValue(ComposterBlock.LEVEL, 7));
        assertEquals(0, input.getSlots());
        assertEquals(0, output.getSlots());
        state.set(empty.setValue(ComposterBlock.LEVEL, 8));
        assertEquals(0, input.getSlots());
        assertEquals(1, output.getSlots());
        assertTrue(output.extractItem(0, 1, true).is(Items.BONE_MEAL));
        state.set(Blocks.STONE.defaultBlockState());
        assertEquals(0, output.getSlots());
        state.set(empty.setValue(ComposterBlock.LEVEL, 8));
        when(level.hasChunk(0, 0)).thenReturn(false);
        assertEquals(0, output.getSlots());
    }

    @Test
    void nativeFluidCapabilityRoundTripsTaggedContentsAndClearsAnEmptyTank() {
        BlockState state = Blocks.FURNACE.defaultBlockState();
        var machine = new Machine(BlockPos.ZERO, state);
        var restored = new Machine(BlockPos.ZERO, state);
        var fluidTag = new CompoundTag();
        fluidTag.putString("origin", "fixture");
        var fluid = new FluidStack(Fluids.LAVA, 740, fluidTag);
        var input = machine.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(AssertionError::new);
        assertEquals(740, input.fill(fluid, FluidAction.EXECUTE));

        var snapshot = machine.saveWithoutMetadata(RegistryAccess.EMPTY);
        assertEquals("minecraft:lava", snapshot.getCompound("Tank").getString("FluidName"));
        var output = restored.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(AssertionError::new);
        restored.loadWithComponents(snapshot, RegistryAccess.EMPTY);
        var loaded = output.getFluidInTank(0);
        assertSame(Fluids.LAVA, loaded.getFluid());
        assertEquals(740, loaded.getAmount());
        assertEquals(fluidTag, loaded.getTag());
        assertEquals(snapshot, restored.saveWithoutMetadata(RegistryAccess.EMPTY));

        input.drain(740, FluidAction.EXECUTE);
        restored.loadWithComponents(machine.saveWithoutMetadata(RegistryAccess.EMPTY), RegistryAccess.EMPTY);
        assertTrue(output.getFluidInTank(0).isEmpty());
    }

    private static final class Machine extends RegistryBlockEntity {
        final ItemStackHandler items = new ItemStackHandler(1);
        final FluidTank tank = new FluidTank(1000);
        final EnergyStorage energy = new EnergyStorage(1000);

        Machine(BlockPos pos, BlockState state) {
            super(BlockEntityType.FURNACE, pos, state);
            exposeCapability(ForgeCapabilities.ITEM_HANDLER, side ->
                    side == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING) ? items : null);
            exposeCapability(ForgeCapabilities.FLUID_HANDLER, side -> tank);
            exposeCapability(ForgeCapabilities.ENERGY, side ->
                    side == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING) ? energy : null);
        }

        @Override
        protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
            super.saveAdditional(output, registries);
            output.put("Tank", tank.writeToNBT(new CompoundTag()));
        }

        @Override
        protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
            super.loadAdditional(input, registries);
            if (input.contains("Tank")) {
                tank.readFromNBT(input.getCompound("Tank"));
            }
        }
    }
}
