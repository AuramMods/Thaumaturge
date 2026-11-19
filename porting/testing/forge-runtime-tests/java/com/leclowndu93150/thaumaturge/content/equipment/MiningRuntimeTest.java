package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.blockinteraction.BlockBreakAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

class MiningRuntimeTest {
    private static final BlockPos POS = new BlockPos(11, 64, -7);

    private enum Denial {
        GAME_MODE, HELD_ITEM, OPERATOR_BLOCK, EVENT_LISTENER
    }

    @ParameterizedTest
    @EnumSource(Denial.class)
    void deniedBreaksPostTheNativeEventAndSendExactlyOneCorrection(Denial denial) throws Exception {
        ServerLevel level = mock(ServerLevel.class);
        GameType mode = denial == Denial.GAME_MODE ? GameType.SPECTATOR : GameType.SURVIVAL;
        ServerPlayer player = serverPlayer(mode);
        BlockState state = (denial == Denial.OPERATOR_BLOCK ? Blocks.COMMAND_BLOCK : Blocks.STONE).defaultBlockState();
        when(player.blockActionRestricted(level, POS, mode)).thenCallRealMethod();
        if (denial == Denial.HELD_ITEM) {
            when(player.getMainHandItem()).thenReturn(new ItemStack(Items.IRON_SWORD));
            when(player.isCreative()).thenReturn(true);
        }
        List<Boolean> seen = new ArrayList<>();
        Consumer<BreakEvent> protect = event -> {
            assertSame(player, event.getPlayer());
            assertSame(level, event.getLevel());
            assertSame(state, event.getState());
            assertEquals(POS, event.getPos());
            seen.add(event.isCanceled());
            if (denial == Denial.EVENT_LISTENER) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, true, BreakEvent.class, protect);
        try {
            assertFalse(BlockBreakAccess.mayBreak(level, player, POS, state));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protect);
        }
        assertEquals(List.of(denial != Denial.EVENT_LISTENER), seen);
        var packet = ArgumentCaptor.forClass(ClientboundBlockUpdatePacket.class);
        verify(player.connection).send(packet.capture());
        assertSame(state, packet.getValue().getBlockState());
        assertEquals(POS, packet.getValue().getPos());
        verifyNoMoreInteractions(player.connection);
    }

    @Test
    void allowedBreakDoesNotSendAReplacementPacket() throws Exception {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = serverPlayer(GameType.SPECTATOR);
        when(player.blockActionRestricted(level, POS, GameType.SPECTATOR)).thenCallRealMethod();
        Consumer<BreakEvent> allow = event -> {
            assertTrue(event.isCanceled());
            event.setCanceled(false);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, true, BreakEvent.class, allow);
        try {
            assertTrue(BlockBreakAccess.mayBreak(level, player, POS, Blocks.STONE.defaultBlockState()));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(allow);
        }
        verifyNoInteractions(player.connection);
    }

    @Test
    void equipmentHarvestPassesWorldAndPositionToTheBlockAndDropsAfterRemoval() throws Exception {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = serverPlayer(GameType.SURVIVAL);
        BlockState state = mock(BlockState.class);
        BlockEntity entity = mock(BlockEntity.class);
        ItemStack held = new ItemStack(Items.IRON_PICKAXE);
        when(level.getBlockState(POS)).thenReturn(state);
        when(level.getBlockEntity(POS)).thenReturn(entity);
        when(player.getMainHandItem()).thenReturn(held);
        when(state.canHarvestBlock(level, POS, player)).thenReturn(true);
        List<String> effects = new ArrayList<>();
        when(level.removeBlock(POS, false)).thenAnswer(call -> {
            effects.add("remove");
            return true;
        });

        try (var blocks = mockStatic(Block.class)) {
            blocks.when(() -> Block.dropResources(state, level, POS, entity, player, held)).thenAnswer(call -> {
                effects.add("drop");
                return null;
            });
            assertTrue(EnchantMining.harvestBlock(level, player, POS, true));
            var order = inOrder(level, state);
            order.verify(level).getBlockEntity(POS);
            order.verify(state).canHarvestBlock(level, POS, player);
            order.verify(level).removeBlock(POS, false);
            blocks.verify(() -> Block.dropResources(state, level, POS, entity, player, held));
        }
        assertEquals(List.of("remove", "drop"), effects);
    }

    @Test
    void copiedLootToolUpgradesFortuneAndPreservesTheHeldStack() {
        ServerLevel level = mock(ServerLevel.class);
        Player player = mock(Player.class);
        ItemStack held = new ItemStack(Items.IRON_PICKAXE);
        held.setDamageValue(23);
        held.enchant(Enchantments.BLOCK_FORTUNE, 2);
        held.enchant(Enchantments.BLOCK_EFFICIENCY, 3);
        when(player.getMainHandItem()).thenReturn(held);

        ItemStack copied = EnchantMining.fortuneTool(level, player, true, 4);
        assertNotSame(held, copied);
        assertEquals(23, copied.getDamageValue());
        assertEquals(4, copied.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));
        assertEquals(1, copied.getEnchantmentLevel(Enchantments.SILK_TOUCH));
        assertEquals(3, copied.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY));
        assertEquals(3, copied.getEnchantmentTags().size());
        assertEquals(2, held.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));
        assertEquals(0, held.getEnchantmentLevel(Enchantments.SILK_TOUCH));
        assertEquals(2, EnchantMining.fortuneTool(level, player, false, 1)
                .getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));

        when(player.getMainHandItem()).thenReturn(ItemStack.EMPTY);
        assertTrue(EnchantMining.fortuneTool(level, player, false, 0).isEmpty());
        ItemStack fallback = EnchantMining.fortuneTool(level, player, false, 300);
        assertTrue(fallback.is(Items.NETHERITE_PICKAXE));
        assertEquals(255, fallback.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));
    }

    private static ServerPlayer serverPlayer(GameType mode) throws Exception {
        ServerPlayer player = mock(ServerPlayer.class);
        var gameMode = mock(ServerPlayerGameMode.class);
        when(gameMode.getGameModeForPlayer()).thenReturn(mode);
        var field = ServerPlayer.class.getField("gameMode");
        field.setAccessible(true);
        field.set(player, gameMode);
        when(player.getMainHandItem()).thenReturn(ItemStack.EMPTY);
        when(player.getAbilities()).thenReturn(new Abilities());
        player.connection = mock(ServerGamePacketListenerImpl.class);
        return player;
    }
}
