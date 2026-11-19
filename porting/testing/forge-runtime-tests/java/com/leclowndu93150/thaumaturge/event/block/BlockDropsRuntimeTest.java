package com.leclowndu93150.thaumaturge.event.block;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraftforge.common.MinecraftForge;
import org.junit.jupiter.api.Test;

class BlockDropsRuntimeTest {
    private static final BlockPos POS = new BlockPos(3, 72, 5);

    @Test
    void harvestPostsNativeEntitiesAndSpawnsTheEditedDropsBeforeAfterBreak() throws Exception {
        var level = level();
        var blockEntity = mock(BlockEntity.class);
        var breaker = mock(Entity.class);
        var tool = new ItemStack(Items.IRON_PICKAXE);
        var state = state(new ItemStack(Items.COAL, 2), new ItemStack(Items.STICK));
        var spawned = recordSpawns(level);
        var bonus = new ItemEntity(level, POS.getX(), POS.getY(), POS.getZ(), new ItemStack(Items.QUARTZ));
        var edited = new ArrayList<ItemEntity>();
        Consumer<BlockDropsEvent> edit = event -> {
            assertSame(level, event.getLevel());
            assertEquals(POS, event.getPos());
            assertSame(state, event.getState());
            assertSame(blockEntity, event.getBlockEntity());
            assertSame(breaker, event.getBreaker());
            assertSame(tool, event.getTool());
            assertTrue(spawned.isEmpty());

            var drops = event.getDrops();
            assertEquals(2, drops.size());
            var coal = drops.get(0);
            assertTrue(coal.getItem().is(Items.COAL));
            assertEquals(2, coal.getItem().getCount());
            var data = new CompoundTag();
            coal.addAdditionalSaveData(data);
            assertEquals(10, data.getShort("PickupDelay"));
            drops.remove(1);
            coal.setItem(new ItemStack(Items.IRON_NUGGET, 3));
            drops.add(bonus);
            edited.addAll(drops);
        };
        withListener(edit, () -> Block.dropResources(state, level, POS, blockEntity, breaker, tool, false));

        assertEquals(2, edited.size());
        assertEquals(edited, spawned);
        assertTrue(spawned.get(0).getItem().is(Items.IRON_NUGGET));
        assertEquals(3, spawned.get(0).getItem().getCount());
        var order = inOrder(level, state);
        order.verify(level).addFreshEntity(spawned.get(0));
        order.verify(level).addFreshEntity(bonus);
        order.verify(state).spawnAfterBreak(level, POS, tool, false);
    }

    @Test
    void cancellationSkipsDropsAndThePostBreakCallback() throws Exception {
        var level = level();
        var blockEntity = mock(BlockEntity.class);
        var state = state(new ItemStack(Items.COAL));
        var spawned = recordSpawns(level);
        var seen = new ArrayList<BlockDropsEvent>();
        Consumer<BlockDropsEvent> cancel = event -> {
            assertSame(blockEntity, event.getBlockEntity());
            assertNull(event.getBreaker());
            assertSame(ItemStack.EMPTY, event.getTool());
            assertEquals(1, event.getDrops().size());
            seen.add(event);
            event.setCanceled(true);
        };
        withListener(cancel, () -> {
            Block.dropResources(state, level, POS, blockEntity);
            assertTrue(spawned.isEmpty());
            Block.popResource(level, POS, new ItemStack(Items.FLINT));
        });

        assertEquals(1, seen.size());
        assertEquals(1, spawned.size());
        assertTrue(spawned.get(0).getItem().is(Items.FLINT));
        verify(state, never()).spawnAfterBreak(level, POS, ItemStack.EMPTY, true);
    }

    @Test
    void nestedBreaksKeepTheirDropsAndHonourTheDefaultExperienceFlag() throws Exception {
        var level = level();
        var outer = state(new ItemStack(Items.COAL));
        var inner = state(new ItemStack(Items.FLINT));
        var innerPos = POS.above();
        var tool = new ItemStack(Items.IRON_PICKAXE);
        var spawned = recordSpawns(level);
        var seen = new ArrayList<BlockPos>();
        Consumer<BlockDropsEvent> nest = event -> {
            seen.add(event.getPos());
            assertEquals(1, event.getDrops().size());
            if (event.getPos().equals(POS)) {
                assertSame(ItemStack.EMPTY, event.getTool());
                Block.dropResources(inner, level, innerPos, null, null, tool);
                assertTrue(event.getDrops().get(0).getItem().is(Items.COAL));
            } else {
                assertEquals(innerPos, event.getPos());
                assertSame(tool, event.getTool());
                assertTrue(event.getDrops().get(0).getItem().is(Items.FLINT));
            }
        };
        withListener(nest, () -> Block.dropResources(outer, level, POS));

        assertEquals(List.of(POS, innerPos), seen);
        assertEquals(2, spawned.size());
        assertTrue(spawned.get(0).getItem().is(Items.FLINT));
        assertTrue(spawned.get(1).getItem().is(Items.COAL));
        verify(inner).spawnAfterBreak(level, innerPos, tool, true);
        verify(outer).spawnAfterBreak(level, POS, ItemStack.EMPTY, true);
    }

    @Test
    void nativeDropRuleAndSnapshotsSuppressNativeItems() throws Exception {
        var level = level();
        var state = state(new ItemStack(Items.COAL));
        var gameRules = mock(GameRules.class);
        when(level.getGameRules()).thenReturn(gameRules);
        var spawned = recordSpawns(level);
        var dropCounts = new ArrayList<Integer>();
        Consumer<BlockDropsEvent> observe = event -> dropCounts.add(event.getDrops().size());
        withListener(observe, () -> {
            Block.dropResources(state, level, POS);
            when(gameRules.getBoolean(GameRules.RULE_DOBLOCKDROPS)).thenReturn(true);
            level.restoringBlockSnapshots = true;
            Block.dropResources(state, level, POS);
        });

        assertEquals(List.of(0, 0), dropCounts);
        assertTrue(spawned.isEmpty());
        verify(state, times(2)).spawnAfterBreak(level, POS, ItemStack.EMPTY, true);
    }

    private static ServerLevel level() throws Exception {
        var level = mock(ServerLevel.class);
        var random = RandomSource.create(138);
        var randomField = Level.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(level, random);
        when(level.getRandom()).thenReturn(random);
        var gameRules = new GameRules();
        when(level.getGameRules()).thenReturn(gameRules);
        return level;
    }

    private static BlockState state(ItemStack... drops) {
        var state = mock(BlockState.class);
        when(state.getDrops(any(LootParams.Builder.class))).thenReturn(List.of(drops));
        return state;
    }

    private static List<ItemEntity> recordSpawns(ServerLevel level) {
        var spawned = new ArrayList<ItemEntity>();
        when(level.addFreshEntity(any(ItemEntity.class))).thenAnswer(call -> spawned.add(call.getArgument(0)));
        return spawned;
    }

    private static void withListener(Consumer<BlockDropsEvent> listener, Runnable action) {
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            action.run();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }
}
