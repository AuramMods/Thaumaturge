package com.leclowndu93150.thaumaturge.testing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public final class BlockCapabilityFixture {
    private BlockCapabilityFixture() {}

    public static BlockEntity provider() {
        var entity = mock(BlockEntity.class);
        when(entity.getCapability(any(), nullable(Direction.class))).thenReturn(LazyOptional.empty());
        return entity;
    }

    public static <T> BlockEntity provider(Capability<T> capability, Direction side, T value) {
        var entity = provider();
        expose(entity, capability, side, value);
        return entity;
    }

    public static <T> BlockEntity expose(Level level, BlockPos pos, Capability<T> capability, Direction side, T value) {
        var entity = provider(capability, side, value);
        when(level.getBlockEntity(pos)).thenReturn(entity);
        return entity;
    }

    public static <T> void expose(BlockEntity entity, Capability<T> capability, Direction side, T value) {
        var optional = value == null ? LazyOptional.<T>empty() : LazyOptional.of(() -> value);
        when(entity.getCapability(capability, side)).thenReturn(optional);
    }
}
