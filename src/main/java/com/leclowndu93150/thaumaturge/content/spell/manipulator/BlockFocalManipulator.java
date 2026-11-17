package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import static com.leclowndu93150.thaumaturge.serialization.BlockCodecs.simpleCodec;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.blockinteraction.BlockInteractions;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.content.workbench.BlockArcaneWorkbench;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockFocalManipulator extends BaseEntityBlock implements BlockInteractions {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return BlockInteractions.use(this, state, level, pos, player, hand, hit);
    }

    public static final MapCodec<BlockFocalManipulator> CODEC = simpleCodec(BlockFocalManipulator::new);

    public BlockFocalManipulator(Properties properties) {
        super(properties);
    }

    protected MapCodec<BlockFocalManipulator> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BlockArcaneWorkbench.SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityFocalManipulator(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TTBlockEntities.FOCAL_MANIPULATOR.get()) {
            return null;
        }
        return level.isClientSide()
                ? createTickerHelper(
                        type, TTBlockEntities.FOCAL_MANIPULATOR.get(), BlockEntityFocalManipulator::clientTick)
                : createTickerHelper(
                        type, TTBlockEntities.FOCAL_MANIPULATOR.get(), BlockEntityFocalManipulator::serverTick);
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && !DeviceGate.passes(player, TTIds.rl("unlock_auromancy"))) {
            return InteractionResult.CONSUME;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityFocalManipulator table) {
            player.openMenu(table, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof BlockEntityFocalManipulator table) {
            table.dropContents();
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
