package com.leclowndu93150.thaumaturge.content.eldritch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import org.junit.jupiter.api.Test;

class EldritchSpawnRuntimeTest {
    @Test
    void preparePostsNativeFinalizeEventAndPassesListenerChangesToTheMob() {
        var level = mock(ServerLevel.class);
        var mob = mock(Mob.class);
        var type = mock(EntityType.class);
        doReturn(mob).when(type).create(level);
        var at = new BlockPos(4, 72, 9);
        var home = new BlockPos(8, 73, 11);
        var localDifficulty = new DifficultyInstance(Difficulty.NORMAL, 0, 0, 0.0F);
        var modifiedDifficulty = new DifficultyInstance(Difficulty.HARD, 0, 0, 0.0F);
        var groupData = mock(SpawnGroupData.class);
        var tag = new CompoundTag();
        when(level.getCurrentDifficultyAt(at)).thenReturn(localDifficulty);
        var random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.25F);

        Consumer<MobSpawnEvent.FinalizeSpawn> listener = event -> {
            if (event.getEntity() != mob) return;
            assertSame(level, event.getLevel());
            assertSame(localDifficulty, event.getDifficulty());
            assertEquals(MobSpawnType.EVENT, event.getSpawnType());
            assertNull(event.getSpawnData());
            assertNull(event.getSpawnTag());
            event.setDifficulty(modifiedDifficulty);
            event.setSpawnData(groupData);
            event.setSpawnTag(tag);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            assertSame(mob, EldritchSpawns.prepare(level, type, at, home, 12, random).orElseThrow());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        var order = inOrder(mob);
        order.verify(mob).moveTo(4.5, 72.0, 9.5, 90.0F, 0.0F);
        order.verify(mob).finalizeSpawn(level, modifiedDifficulty, MobSpawnType.EVENT, groupData, tag);
        order.verify(mob).setPersistenceRequired();
        order.verify(mob).restrictTo(home, 12);
    }

    @Test
    void canceledFinalizationLeavesThePreparedMobForItsCaller() {
        var level = mock(ServerLevel.class);
        var mob = mock(Mob.class);
        var type = mock(EntityType.class);
        doReturn(mob).when(type).create(level);
        var difficulty = new DifficultyInstance(Difficulty.NORMAL, 0, 0, 0.0F);
        when(level.getCurrentDifficultyAt(BlockPos.ZERO)).thenReturn(difficulty);
        Consumer<MobSpawnEvent.FinalizeSpawn> listener = event -> {
            if (event.getEntity() == mob) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            assertSame(mob, EldritchSpawns.prepare(level, type, BlockPos.ZERO, BlockPos.ZERO, 12,
                    RandomSource.create(19)).orElseThrow());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        verify(mob, never()).finalizeSpawn(any(), any(), any(), nullable(SpawnGroupData.class), nullable(CompoundTag.class));
        verify(mob).setPersistenceRequired();
        verify(mob).restrictTo(BlockPos.ZERO, 12);
    }
}
