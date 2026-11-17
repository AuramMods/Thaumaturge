package com.leclowndu93150.thaumaturge.blockinteraction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BlockInteractionsTest {
    private final Player player = mock(Player.class);
    private final Level level = mock(Level.class);
    private final BlockState state = mock(BlockState.class);
    private final BlockPos pos = new BlockPos(4, 70, -6);
    private final BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

    private class Fixture implements BlockInteractions {
        final List<String> phases = new ArrayList<>();
        ItemInteractionResult item = ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        InteractionResult empty = InteractionResult.SUCCESS;
        ItemStack receivedStack;
        InteractionHand receivedHand;

        @Override
        public ItemInteractionResult useItemOn(ItemStack stack, BlockState actualState, Level actualLevel,
                BlockPos actualPos, Player actualPlayer, InteractionHand hand, BlockHitResult actualHit) {
            phases.add("item");
            assertSame(state, actualState);
            assertSame(level, actualLevel);
            assertSame(pos, actualPos);
            assertSame(player, actualPlayer);
            assertSame(hit, actualHit);
            receivedStack = stack;
            receivedHand = hand;
            return item;
        }

        @Override
        public InteractionResult useWithoutItem(BlockState actualState, Level actualLevel,
                BlockPos actualPos, Player actualPlayer, BlockHitResult actualHit) {
            phases.add("default");
            assertSame(state, actualState);
            assertSame(level, actualLevel);
            assertEquals(pos, actualPos);
            assertSame(player, actualPlayer);
            assertSame(hit, actualHit);
            return empty;
        }
    }

    static Stream<Arguments> itemPhaseCases() {
        return Stream.of(
                Arguments.of(ItemInteractionResult.SUCCESS, InteractionHand.MAIN_HAND, InteractionResult.SUCCESS, false),
                Arguments.of(ItemInteractionResult.CONSUME, InteractionHand.MAIN_HAND, InteractionResult.CONSUME, false),
                Arguments.of(ItemInteractionResult.CONSUME_PARTIAL, InteractionHand.MAIN_HAND, InteractionResult.CONSUME_PARTIAL, false),
                Arguments.of(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION, InteractionHand.MAIN_HAND, InteractionResult.SUCCESS, true),
                Arguments.of(ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION, InteractionHand.MAIN_HAND, InteractionResult.PASS, false),
                Arguments.of(ItemInteractionResult.FAIL, InteractionHand.MAIN_HAND, InteractionResult.PASS, false),
                Arguments.of(ItemInteractionResult.SUCCESS, InteractionHand.OFF_HAND, InteractionResult.SUCCESS, false),
                Arguments.of(ItemInteractionResult.CONSUME, InteractionHand.OFF_HAND, InteractionResult.CONSUME, false),
                Arguments.of(ItemInteractionResult.CONSUME_PARTIAL, InteractionHand.OFF_HAND, InteractionResult.CONSUME_PARTIAL, false),
                Arguments.of(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION, InteractionHand.OFF_HAND, InteractionResult.PASS, false),
                Arguments.of(ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION, InteractionHand.OFF_HAND, InteractionResult.PASS, false),
                Arguments.of(ItemInteractionResult.FAIL, InteractionHand.OFF_HAND, InteractionResult.PASS, false));
    }

    @ParameterizedTest
    @MethodSource("itemPhaseCases")
    void preservesPhaseOrderAndMainHandOnlyFallback(ItemInteractionResult item, InteractionHand hand,
            InteractionResult expected, boolean defaultCalled) {
        var block = new Fixture();
        block.item = item;
        when(player.getItemInHand(hand)).thenReturn(ItemStack.EMPTY);
        assertSame(expected, BlockInteractions.use(block, state, level, pos, player, hand, hit));
        assertEquals(defaultCalled ? List.of("item", "default") : List.of("item"), block.phases);
        assertSame(ItemStack.EMPTY, block.receivedStack);
        assertSame(hand, block.receivedHand);
    }

    static Stream<Arguments> defaultPhaseCases() {
        return Stream.of(
                Arguments.of(InteractionResult.SUCCESS, InteractionResult.SUCCESS),
                Arguments.of(InteractionResult.CONSUME, InteractionResult.CONSUME),
                Arguments.of(InteractionResult.CONSUME_PARTIAL, InteractionResult.CONSUME_PARTIAL),
                Arguments.of(InteractionResult.PASS, InteractionResult.PASS),
                Arguments.of(InteractionResult.FAIL, InteractionResult.PASS));
    }

    @ParameterizedTest
    @MethodSource("defaultPhaseCases")
    void preservesConsumingResultsAndContinuesHeldItemUseOtherwise(InteractionResult result, InteractionResult expected) {
        var block = new Fixture();
        block.empty = result;
        assertSame(expected, BlockInteractions.use(block, state, level, pos, player, InteractionHand.MAIN_HAND, hit));
        assertEquals(List.of("item", "default"), block.phases);
    }

    @Test
    void usesActualOffHandStackAndRetainsInheritedOverrides() {
        ItemStack main = mock(ItemStack.class);
        ItemStack off = mock(ItemStack.class);
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(main);
        when(player.getItemInHand(InteractionHand.OFF_HAND)).thenReturn(off);
        var derived = new Fixture() {};
        derived.item = ItemInteractionResult.SUCCESS;
        BlockInteractions.use(derived, state, level, pos, player, InteractionHand.OFF_HAND, hit);
        assertSame(off, derived.receivedStack);
        assertEquals(List.of("item"), derived.phases);
    }

    @Test
    void interfaceDefaultsAllowTheNativeItemPipelineToContinue() {
        BlockInteractions block = new BlockInteractions() {};
        assertSame(InteractionResult.PASS,
                BlockInteractions.use(block, state, level, pos, player, InteractionHand.MAIN_HAND, hit));
    }

    @Test
    void directForwardingCallsOnlyTheHostsDefaultPhase() {
        Block block = mock(Block.class, withSettings().extraInterfaces(BlockInteractions.class));
        when(state.getBlock()).thenReturn(block);
        var interaction = (BlockInteractions) block;
        when(interaction.useWithoutItem(state, level, pos, player, hit)).thenReturn(InteractionResult.FAIL);
        assertSame(InteractionResult.FAIL, BlockInteractions.useWithoutItem(state, level, player, hit));
        verify(interaction).useWithoutItem(state, level, pos, player, hit);
        verify(interaction, never()).useItemOn(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void forwardingToANativeBlockUsesItsNativeMainHandCallback() {
        when(state.getBlock()).thenReturn(mock(Block.class));
        when(state.use(level, player, InteractionHand.MAIN_HAND, hit)).thenReturn(InteractionResult.CONSUME);
        assertSame(InteractionResult.CONSUME, BlockInteractions.useWithoutItem(state, level, player, hit));
    }

    @Test
    void callbackFailuresPropagateWithoutRunningTheDefaultPhase() {
        var block = new Fixture() {
            @Override
            public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                    Player player, InteractionHand hand, BlockHitResult hit) {
                throw new IllegalStateException("broken interaction");
            }
        };
        assertThrows(IllegalStateException.class,
                () -> BlockInteractions.use(block, state, level, pos, player, InteractionHand.MAIN_HAND, hit));
        assertTrue(block.phases.isEmpty());
    }

    @Test
    void sidedSuccessRetainsNativeSwingAndConsumptionResults() {
        assertSame(ItemInteractionResult.SUCCESS, ItemInteractionResult.sidedSuccess(true));
        assertSame(ItemInteractionResult.CONSUME, ItemInteractionResult.sidedSuccess(false));
        assertFalse(ItemInteractionResult.FAIL.consumesAction());
        assertSame(InteractionResult.FAIL, ItemInteractionResult.FAIL.result());
        assertTrue(ItemInteractionResult.CONSUME_PARTIAL.consumesAction());
        assertSame(InteractionResult.PASS, ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION.result());
    }
}
