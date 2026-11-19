package com.leclowndu93150.thaumaturge.api.items;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvHelperConsumptionCutoffTest {
    private Level level;
    private Player player;
    private Inventory inventory;
    private ItemStackHandler chest;
    private BlockEntity adjacent;

    @BeforeEach
    void setUp() {
        level = mock(Level.class);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        player = mock(Player.class);
        inventory = new Inventory(player);
        when(player.getInventory()).thenReturn(inventory);
        chest = new ItemStackHandler(1);
        adjacent = mock(BlockEntity.class);
        when(level.getBlockEntity(BlockPos.ZERO.below())).thenReturn(adjacent);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        expose(chest);
    }

    private void expose(IItemHandler handler) {
        when(adjacent.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP))
                .thenReturn(LazyOptional.of(() -> handler));
    }

    @Test
    void partialAdjacentSupplyConsumesOnlyThePlayersShortfall() {
        chest.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 1));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 4));
        ItemStack wanted = new ItemStack(Items.IRON_INGOT, 4);
        assertTrue(InvHelper.takeFromNeighboursOrPlayer(level, BlockPos.ZERO, player, false, wanted));
        assertTrue(chest.getStackInSlot(0).isEmpty());
        assertEquals(1, inventory.getItem(0).getCount());
        assertEquals(4, wanted.getCount());
    }

    @Test
    void completeAdjacentSupplyLeavesThePlayerUntouchedAndSimulationMutatesNeither() {
        chest.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 4));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 7));
        ItemStack wanted = new ItemStack(Items.IRON_INGOT, 4);
        assertTrue(InvHelper.takeFromNeighboursOrPlayer(level, BlockPos.ZERO, player, true, wanted));
        assertEquals(4, chest.getStackInSlot(0).getCount());
        assertEquals(7, inventory.getItem(0).getCount());
        assertTrue(InvHelper.takeFromNeighboursOrPlayer(level, BlockPos.ZERO, player, false, wanted));
        assertTrue(chest.getStackInSlot(0).isEmpty());
        assertEquals(7, inventory.getItem(0).getCount());
    }

    @Test
    void nativePreflightKeepsCutoffRequirementForOneFullSourceAndRejectsWithoutMutation() {
        chest.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 2));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        ItemStack wanted = new ItemStack(Items.IRON_INGOT, 4);
        assertFalse(InvHelper.takeFromNeighboursOrPlayer(level, BlockPos.ZERO, player, false, wanted));
        assertEquals(2, chest.getStackInSlot(0).getCount());
        assertEquals(2, inventory.getItem(0).getCount());
    }

    @Test
    void refusingProviderIsNotReportedAsSuccessfulConsumption() {
        IItemHandler refusing = mock(IItemHandler.class);
        when(refusing.getSlots()).thenReturn(1);
        when(refusing.getStackInSlot(0)).thenReturn(new ItemStack(Items.IRON_INGOT, 4));
        when(refusing.extractItem(anyInt(), anyInt(), anyBoolean())).thenReturn(ItemStack.EMPTY);
        expose(refusing);
        assertFalse(InvHelper.takeFromNeighboursOrPlayer(
                level, BlockPos.ZERO, player, false, new ItemStack(Items.IRON_INGOT, 4)));
        assertTrue(inventory.getItem(0).isEmpty());
    }

    @Test
    void competingIngredientsFailWithoutMutatingEitherSource() {
        chest.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 5));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 2));

        assertFalse(InvHelper.takeFromNeighboursOrPlayer(
                level,
                BlockPos.ZERO,
                player,
                false,
                new ItemStack(Items.IRON_INGOT, 4),
                new ItemStack(Items.IRON_INGOT, 4)));

        assertEquals(5, chest.getStackInSlot(0).getCount());
        assertEquals(2, inventory.getItem(0).getCount());
    }

    @Test
    void defaultToolRequestAllowsNameDataButAnExplicitNameStillMatches() {
        var named = new ItemStack(Items.IRON_PICKAXE).setHoverName(Component.literal("Named"));
        inventory.setItem(0, named);
        var ordinary = new ItemStack(Items.IRON_PICKAXE);
        var other = new ItemStack(Items.IRON_PICKAXE).setHoverName(Component.literal("Other"));
        assertTrue(InvHelper.playerHolds(player, ordinary, false));
        assertTrue(InvHelper.playerHolds(player, named.copy(), false));
        assertFalse(InvHelper.playerHolds(player, other, false));
    }

    @Test
    void unexpectedShortExtractionRollsBackNeighborAndPlayerTogether() {
        ItemStackHandler shortExtracting = new ItemStackHandler(1) {
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return super.extractItem(slot, Math.max(0, amount - 1), simulate);
            }
        };
        shortExtracting.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 3));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 4));
        expose(shortExtracting);

        assertFalse(InvHelper.takeFromNeighboursOrPlayer(
                level, BlockPos.ZERO, player, false, new ItemStack(Items.IRON_INGOT, 4)));

        assertEquals(3, shortExtracting.getStackInSlot(0).getCount());
        assertEquals(4, inventory.getItem(0).getCount());
    }
}
