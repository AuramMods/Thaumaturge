package com.leclowndu93150.thaumaturge.content.taint.spread;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.compat.BlockCollisionChecks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;

class TaintSeedBlockClearanceRuntimeTest {
    @Test
    void seedClearanceUsesNativeBlockShapesIndependentlyOfOtherEntities() {
        var level = mock(CollisionGetter.class, CALLS_REAL_METHODS);
        var chunk = mock(BlockGetter.class);
        when(level.getChunkForCollisions(anyInt(), anyInt())).thenReturn(chunk);
        var actor = mock(Entity.class);
        var bounds = new AABB(0.2, 64.6, 0.2, 0.8, 65.4, 0.8);
        doReturn(List.of(Shapes.create(bounds))).when(level).getEntityCollisions(actor, bounds);

        var base = new BlockPos(0, 64, 0);
        when(chunk.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        when(chunk.getBlockState(base)).thenReturn(Blocks.OAK_SLAB.defaultBlockState());
        assertTrue(BlockCollisionChecks.hasNoBlockCollision(level, actor, bounds));
        verify(level, never()).getEntityCollisions(actor, bounds);
        assertFalse(level.noCollision(actor, bounds));

        when(chunk.getBlockState(base)).thenReturn(Blocks.STONE.defaultBlockState());
        assertFalse(BlockCollisionChecks.hasNoBlockCollision(level, actor, bounds));
    }
}
