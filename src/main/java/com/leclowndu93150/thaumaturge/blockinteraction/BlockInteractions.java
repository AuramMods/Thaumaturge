package com.leclowndu93150.thaumaturge.blockinteraction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Preserves the mod's two interaction phases inside native Forge Block#use. */
public interface BlockInteractions {
    default ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    default InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    /** Forge owns spectator, event, sneak-bypass and item-use handling around this call. */
    static InteractionResult use(BlockInteractions block, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        ItemInteractionResult item = block.useItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hit);
        if (item.consumesAction()) return item.result();
        if (item == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == InteractionHand.MAIN_HAND) {
            InteractionResult result = block.useWithoutItem(state, level, pos, player, hit);
            if (result.consumesAction()) return result;
        }
        // The 1.21 player pipeline continues to the held item's useOn for every
        // non-consuming block result, including FAIL. Native Forge does so on PASS.
        return InteractionResult.PASS;
    }

    /** Explicit default-phase forwarding, used by the workbench charger. */
    static InteractionResult useWithoutItem(BlockState state, Level level, Player player, BlockHitResult hit) {
        if (state.getBlock() instanceof BlockInteractions block) {
            return block.useWithoutItem(state, level, hit.getBlockPos(), player, hit);
        }
        return state.use(level, player, InteractionHand.MAIN_HAND, hit);
    }
}
