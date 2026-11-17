package com.leclowndu93150.thaumaturge.content.eldritch.block;

import static com.leclowndu93150.thaumaturge.serialization.BlockCodecs.simpleCodec;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockEldritchStructure extends Block {
    public static final MapCodec<BlockEldritchStructure> CODEC = simpleCodec(BlockEldritchStructure::new);
    public static final BooleanProperty SEALED = BooleanProperty.create("sealed");

    public BlockEldritchStructure(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SEALED, Boolean.FALSE));
    }

    protected MapCodec<? extends BlockEldritchStructure> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEALED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return state.getValue(SEALED) ? 0.0F : super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getValue(SEALED) ? Float.MAX_VALUE : super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return !state.getValue(SEALED) && super.canEntityDestroy(state, level, pos, entity);
    }
}
