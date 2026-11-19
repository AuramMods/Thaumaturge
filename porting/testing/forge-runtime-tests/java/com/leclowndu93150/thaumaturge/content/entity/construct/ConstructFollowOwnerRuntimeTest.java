package com.leclowndu93150.thaumaturge.content.entity.construct;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConstructFollowOwnerRuntimeTest {
    @Test
    void goalTemporarilyAllowsWaterAndRestoresTheNativePathfindingCost() {
        var construct = mock(EntityOwnedConstruct.class);
        var navigation = mock(PathNavigation.class);
        var owner = mock(LivingEntity.class);
        when(construct.getNavigation()).thenReturn(navigation);
        when(construct.getOwner()).thenReturn(owner);
        when(construct.distanceTo(owner)).thenReturn(8.0F);
        when(construct.getPathfindingMalus(BlockPathTypes.WATER)).thenReturn(7.5F);

        var goal = new ConstructFollowOwnerGoal(construct, 1.0, 6.0F, 3.0F);
        assertTrue(goal.canUse());
        goal.start();
        verify(construct).setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        assertTrue(goal.canContinueToUse());
        goal.stop();
        verify(navigation).stop();
        verify(construct).setPathfindingMalus(BlockPathTypes.WATER, 7.5F);
        assertFalse(goal.canContinueToUse());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void failedPathUsesTheFirstSafeColumnOnlyWhenUnleashed(boolean leashed) {
        var construct = mock(EntityOwnedConstruct.class);
        var navigation = mock(PathNavigation.class);
        var owner = mock(LivingEntity.class);
        var level = mock(ServerLevel.class);
        var safe = new BlockPos(18, 70, 21);
        when(construct.getOwner()).thenReturn(owner);
        when(construct.distanceTo(owner)).thenReturn(14.0F);
        when(construct.getNavigation()).thenReturn(navigation);
        when(construct.getLookControl()).thenReturn(mock(LookControl.class));
        when(construct.level()).thenReturn(level);
        when(construct.isLeashed()).thenReturn(leashed);
        when(construct.getYRot()).thenReturn(35.0F);
        when(construct.getXRot()).thenReturn(-6.0F);
        when(owner.blockPosition()).thenReturn(new BlockPos(20, 70, 22));
        when(level.getBlockState(org.mockito.ArgumentMatchers.any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getBlockState(safe.below())).thenReturn(Blocks.STONE.defaultBlockState());

        var goal = new ConstructFollowOwnerGoal(construct, 1.1, 6.0F, 3.0F);
        assertTrue(goal.canUse());
        goal.start();
        goal.tick();
        verify(navigation).moveTo(owner, 1.1);
        if (leashed) {
            verify(construct, never()).moveTo(anyDouble(), anyDouble(), anyDouble(), anyFloat(), anyFloat());
        } else {
            verify(construct).moveTo(18.5, 70.0, 21.5, 35.0F, -6.0F);
            verify(navigation).stop();
        }
    }
}
