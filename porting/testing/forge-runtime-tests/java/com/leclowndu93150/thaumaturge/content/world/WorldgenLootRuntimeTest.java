package com.leclowndu93150.thaumaturge.content.world;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.junit.jupiter.api.Test;

class WorldgenLootRuntimeTest {
    @Test
    void nativeWorldgenChestPersistsTheDungeonTableAndConsumesOneSeed() {
        var level = mock(WorldGenLevel.class);
        var random = mock(RandomSource.class);
        var pos = new BlockPos(19, 87, -23);
        var state = Blocks.CHEST.defaultBlockState();
        var chest = new ChestBlockEntity(pos, state);
        when(level.getBlockEntity(pos)).thenReturn(chest);
        when(random.nextLong()).thenReturn(713L);

        RandomizableContainerBlockEntity.setLootTable(level, random, pos.below(), BuiltInLootTables.SIMPLE_DUNGEON);
        verify(random, never()).nextLong();
        RandomizableContainerBlockEntity.setLootTable(level, random, pos, BuiltInLootTables.SIMPLE_DUNGEON);
        verify(random).nextLong();

        var saved = chest.saveWithFullMetadata();
        assertEquals("minecraft:chests/simple_dungeon", saved.getString(RandomizableContainerBlockEntity.LOOT_TABLE_TAG));
        assertEquals(713L, saved.getLong(RandomizableContainerBlockEntity.LOOT_TABLE_SEED_TAG));
        var loaded = assertInstanceOf(ChestBlockEntity.class, BlockEntity.loadStatic(pos, state, saved));
        assertEquals(saved, loaded.saveWithFullMetadata());
    }

    @Test
    void shortGrassUsesTheTargetBlockAndItemIdWithTheSourceAspects() throws Exception {
        var grass = Blocks.GRASS.defaultBlockState();
        var encoded = BlockState.CODEC.encodeStart(JsonOps.INSTANCE, grass).result().orElseThrow();
        assertEquals("minecraft:grass", encoded.getAsJsonObject().get("Name").getAsString());
        assertTrue(grass.canBeReplaced());

        var sourceResources = Path.of(System.getProperty("thaumaturge.testResourceRoot"));
        var file = sourceResources.resolve("data/thaumaturge/data_maps/item/base_aspects.json");
        var values = JsonParser.parseString(Files.readString(file)).getAsJsonObject().getAsJsonObject("values");
        var itemId = BuiltInRegistries.ITEM.getKey(Blocks.GRASS.asItem()).toString();
        assertEquals("minecraft:grass", itemId);
        assertEquals(JsonParser.parseString("""
                [{"aspect":"thaumaturge:herba","amount":2},{"aspect":"thaumaturge:aer","amount":1}]
                """), values.get(itemId));
    }
}
