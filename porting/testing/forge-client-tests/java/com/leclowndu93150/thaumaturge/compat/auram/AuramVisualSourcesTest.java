package com.leclowndu93150.thaumaturge.compat.auram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class AuramVisualSourcesTest {
    @Test
    void usesTheContainingAirVoxelOrTheNearestOpenNeighbor() {
        var level = mock(ClientLevel.class);
        when(level.isClientSide()).thenReturn(true);
        when(level.hasChunkAt(any())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.STONE.defaultBlockState());

        var point = new Vec3(0.98, 0.5, 0.45);
        var east = BlockPos.ZERO.east();
        var north = BlockPos.ZERO.north();
        assertNull(AuramVisualSources.closestOpenCell(level, point));
        when(level.getBlockState(east)).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getBlockState(north)).thenReturn(Blocks.AIR.defaultBlockState());
        assertEquals(east, AuramVisualSources.closestOpenCell(level, point));

        when(level.getBlockState(BlockPos.ZERO)).thenReturn(Blocks.AIR.defaultBlockState());
        assertEquals(BlockPos.ZERO, AuramVisualSources.closestOpenCell(level, point));
    }
}
