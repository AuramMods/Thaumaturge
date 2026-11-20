package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import static com.leclowndu93150.thaumaturge.serialization.BlockCodecs.simpleCodec;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.blockinteraction.BlockInteractions;
import com.leclowndu93150.thaumaturge.blockinteraction.ItemInteractionResult;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockAdvancedAlchemicalFurnace extends BaseEntityBlock implements BlockInteractions {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return BlockInteractions.use(this, state, level, pos, player, hand, hit);
    }

    public static final MapCodec<BlockAdvancedAlchemicalFurnace> CODEC =
            simpleCodec(BlockAdvancedAlchemicalFurnace::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final ResourceLocation RESEARCH = TTIds.rl("advanced_alchemical_furnace");
    private static final VoxelShape INPUT_COLLISION = Block.box(0.0, 0.0, 0.0, 16.0, 11.2, 16.0);

    public BlockAdvancedAlchemicalFurnace(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    protected MapCodec<BlockAdvancedAlchemicalFurnace> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return INPUT_COLLISION;
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
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!DeviceGate.passes(player, RESEARCH)) {
            return ItemInteractionResult.CONSUME;
        }
        if (!(level.getBlockEntity(pos) instanceof BlockEntityAdvancedAlchemicalFurnace furnace)
                || !furnace.insertInput(stack)) {
            return ItemInteractionResult.FAIL;
        }
        InvHelper.consume(stack, 1, player);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BlockEntityAdvancedAlchemicalFurnace furnace) {
                furnace.dropContents(pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
            if (!level.isClientSide()) {
                AdvancedAlchemicalFurnaceStructure.restore(level, pos);
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAdvancedAlchemicalFurnace(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(
                        type,
                        TTBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(),
                        BlockEntityAdvancedAlchemicalFurnace::serverTick);
    }
}
