package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityEldritchNothing extends RegistryBlockEntity {
    public BlockEntityEldritchNothing(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_NOTHING.get(), pos, state);
    }
}
