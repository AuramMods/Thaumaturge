package com.leclowndu93150.thaumaturge.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.junit.jupiter.api.Test;

class ChunkAttachmentRuntimeTest {
    @Test
    void generationStateSurvivesNativeSaveLoadPromotionAndFullChunkReload() throws Exception {
        var registry = new AttachmentRegistry("port_chunk");
        var stamp = registry.register("labyrinth_stamp",
                AttachmentKey.builder(() -> -1).serialize(Codec.INT, value -> value >= 0));
        var field = AttachmentData.class.getDeclaredField("schemas");
        field.setAccessible(true);
        Object previous = field.get(null);
        field.set(null, registry);
        Consumer<AttachCapabilitiesEvent<LevelChunk>> attach = AttachmentData::attachChunk;
        MinecraftForge.EVENT_BUS.addGenericListener(LevelChunk.class, attach);
        try {
            var level = level();
            var access = level.registryAccess();
            var poi = mock(PoiManager.class);
            var pos = new ChunkPos(2, -1);
            var proto = new ProtoChunk(pos, UpgradeData.EMPTY, level, access.registryOrThrow(Registries.BIOME), null);
            proto.setStatus(ChunkStatus.FEATURES);
            proto.setUnsaved(false);
            assertNull(ChunkAttachmentData.getExisting(proto, stamp));
            assertFalse(proto.isUnsaved());
            ChunkAttachmentData.set(proto, access, stamp, 17);
            assertTrue(proto.isUnsaved());

            var savedGeneration = ChunkSerializer.write(level, proto);
            var protoTag = savedGeneration.getCompound("thaumaturge:attachments");
            assertEquals(17, protoTag.getInt("port_chunk:labyrinth_stamp"));
            protoTag.putString("addon:opaque", "preserved");
            var reloaded = ChunkSerializer.read(level, poi, pos, savedGeneration);
            assertEquals(17, ChunkAttachmentData.getExisting(reloaded, stamp));

            var full = new LevelChunk(level, reloaded, null);
            assertEquals(17, AttachmentData.get(full, stamp));
            var wrapper = new ImposterProtoChunk(full, false);
            ChunkAttachmentData.set(wrapper, access, stamp, 18);
            assertEquals(18, ChunkAttachmentData.getExisting(wrapper, stamp));

            var savedFull = ChunkSerializer.write(level, full);
            assertFalse(savedFull.contains("thaumaturge:attachments"));
            var caps = savedFull.getCompound("ForgeCaps").getCompound("thaumaturge:state");
            assertEquals(18, caps.getInt("port_chunk:labyrinth_stamp"));
            assertEquals("preserved", caps.getString("addon:opaque"));
            var readFull = ChunkSerializer.read(level, poi, pos, savedFull);
            assertEquals(18, ChunkAttachmentData.getExisting(readFull, stamp));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(attach);
            field.set(null, previous);
        }
    }

    private static ServerLevel level() {
        var vanilla = VanillaRegistries.createLookup();
        var biomes = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        var structures = new MappedRegistry<Structure>(Registries.STRUCTURE, Lifecycle.stable());
        Registry.register(biomes, Biomes.PLAINS, vanilla.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS).value());
        var access = new RegistryAccess.ImmutableRegistryAccess(List.of(biomes, structures)).freeze();
        var level = mock(ServerLevel.class);
        var server = mock(MinecraftServer.class);
        var levelData = mock(LevelData.class);
        var dimension = mock(DimensionType.class);
        var chunks = mock(ServerChunkCache.class);
        var light = mock(ThreadedLevelLightEngine.class);
        when(level.getLevel()).thenReturn(level);
        when(level.getServer()).thenReturn(server);
        when(level.getLevelData()).thenReturn(levelData);
        when(level.dimensionType()).thenReturn(dimension);
        when(level.registryAccess()).thenReturn(access);
        when(level.getHeight()).thenReturn(128);
        when(level.getSectionsCount()).thenReturn(8);
        when(level.getMaxSection()).thenReturn(8);
        when(level.getChunkSource()).thenReturn(chunks);
        when(chunks.getLightEngine()).thenReturn(light);
        when(server.registryAccess()).thenReturn(access);
        return level;
    }
}
