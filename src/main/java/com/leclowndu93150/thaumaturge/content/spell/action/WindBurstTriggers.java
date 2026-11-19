package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.mixin.world.level.block.ButtonBlockSoundInvoker;
import com.leclowndu93150.thaumaturge.mixin.world.level.block.FenceGateBlockSoundsAccessor;
import com.leclowndu93150.thaumaturge.mixin.world.level.block.TrapDoorBlockAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;

final class WindBurstTriggers {
    private WindBurstTriggers() {}

    static void trigger(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof AbstractCandleBlock && state.getValue(AbstractCandleBlock.LIT)) {
            AbstractCandleBlock.extinguish(null, state, level, pos);
        } else if (block instanceof BellBlock bell) {
            bell.attemptToRing(level, pos, null);
        } else if (block instanceof ButtonBlock button && !state.getValue(ButtonBlock.POWERED)) {
            button.press(state, level, pos);
            ((ButtonBlockSoundInvoker) button).thaumaturge$playButtonSound(null, level, pos, true);
            level.gameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
        } else if (block instanceof DoorBlock door && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
                && !state.getValue(DoorBlock.POWERED) && door.type().canOpenByHand()) {
            door.setOpen(null, level, state, pos, !door.isOpen(state));
        } else if (block instanceof FenceGateBlock gate && !state.getValue(FenceGateBlock.POWERED)) {
            toggleGate(level, pos, state, (FenceGateBlockSoundsAccessor) gate);
        } else if (block instanceof LeverBlock lever) {
            boolean powered = lever.pull(state, level, pos).getValue(LeverBlock.POWERED);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, powered ? 0.6F : 0.5F);
            level.gameEvent(null, powered ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
        } else if (block instanceof TrapDoorBlock door && !state.getValue(TrapDoorBlock.POWERED)) {
            toggleTrapDoor(level, pos, state, (TrapDoorBlockAccess) door);
        }
    }

    private static void toggleGate(ServerLevel level, BlockPos pos, BlockState state, FenceGateBlockSoundsAccessor gate) {
        BlockState next = state.cycle(FenceGateBlock.OPEN);
        boolean open = next.getValue(FenceGateBlock.OPEN);
        level.setBlockAndUpdate(pos, next);
        level.playSound(null, pos, open ? gate.thaumaturge$openSound() : gate.thaumaturge$closeSound(),
                SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        level.gameEvent(open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos, GameEvent.Context.of(next));
    }

    private static void toggleTrapDoor(ServerLevel level, BlockPos pos, BlockState state, TrapDoorBlockAccess door) {
        if (!door.thaumaturge$trapDoorType().canOpenByHand()) {
            return;
        }
        BlockState next = state.cycle(TrapDoorBlock.OPEN);
        level.setBlock(pos, next, Block.UPDATE_CLIENTS);
        if (next.getValue(TrapDoorBlock.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        door.thaumaturge$playTrapDoorSound(null, level, pos, next.getValue(TrapDoorBlock.OPEN));
    }
}
