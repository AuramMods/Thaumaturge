package com.leclowndu93150.thaumaturge.content.spa;

import static com.leclowndu93150.thaumaturge.serialization.BlockCodecs.simpleCodec;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.blockinteraction.BlockInteractions;
import com.leclowndu93150.thaumaturge.blockinteraction.ItemInteractionResult;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.network.NetworkHooks;
import org.jspecify.annotations.Nullable;

public class BlockSpa extends BaseEntityBlock implements BlockInteractions {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return BlockInteractions.use(this, state, level, pos, player, hand, hit);
    }

    public static final MapCodec<BlockSpa> CODEC = simpleCodec(BlockSpa::new);

    public BlockSpa(Properties properties) {
        super(properties);
    }

    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntitySpa(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof ServerPlayer player && !(player instanceof FakePlayer)
                && level.getBlockEntity(pos) instanceof BlockEntitySpa spa) {
            spa.setOwner(player.getUUID());
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TTBlockEntities.SPA.get(), BlockEntitySpa::serverTick);
    }

    @Override
    public ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide() && !DeviceGate.passes(player, TTIds.rl("arcane_spa"))) {
            return ItemInteractionResult.CONSUME;
        }
        if (FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof BlockEntitySpa be) {
            NetworkHooks.openScreen(serverPlayer, be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }
}
