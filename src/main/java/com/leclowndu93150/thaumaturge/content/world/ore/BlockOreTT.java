package com.leclowndu93150.thaumaturge.content.world.ore;

import static com.leclowndu93150.thaumaturge.serialization.BlockCodecs.simpleCodec;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockOreTT extends Block {
    public static final MapCodec<BlockOreTT> CODEC = simpleCodec(BlockOreTT::new);

    public BlockOreTT(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected MapCodec<BlockOreTT> codec() {
        return CODEC;
    }
}
