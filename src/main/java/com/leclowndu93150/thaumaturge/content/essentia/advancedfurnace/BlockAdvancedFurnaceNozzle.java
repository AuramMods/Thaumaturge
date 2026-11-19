package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockPlaceholder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockAdvancedFurnaceNozzle extends BlockPlaceholder implements EntityBlock {
    public BlockAdvancedFurnaceNozzle(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAdvancedFurnaceNozzle(pos, state);
    }
}
