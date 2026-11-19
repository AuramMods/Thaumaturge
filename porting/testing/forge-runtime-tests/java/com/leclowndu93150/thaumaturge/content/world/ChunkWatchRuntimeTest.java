package com.leclowndu93150.thaumaturge.content.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkWatchEvent;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.Test;

class ChunkWatchRuntimeTest {
    @Test
    void nativeWatchEventExposesTheExactChunkAfterItIsQueuedForTheWatcher() throws Exception {
        var pos = new ChunkPos(-7, 14);
        var level = mock(ServerLevel.class);
        var chunk = mock(LevelChunk.class);
        var watcher = mock(ServerPlayer.class);
        var packet = mock(ClientboundLevelChunkWithLightPacket.class);
        var packetsQueued = new ArrayList<ClientboundLevelChunkWithLightPacket>();
        when(chunk.getPos()).thenReturn(pos);
        watcher.connection = mock(ServerGamePacketListenerImpl.class);
        doAnswer(call -> {
            packetsQueued.add(call.getArgument(0));
            return null;
        }).when(watcher.connection).send(packet);
        doCallRealMethod().when(watcher).trackChunk(pos, packet);

        var manager = mock(ChunkMap.class);
        var levelField = ChunkMap.class.getDeclaredField("level");
        levelField.setAccessible(true);
        levelField.set(manager, level);
        var entities = ChunkMap.class.getDeclaredField("entityMap");
        entities.setAccessible(true);
        entities.set(manager, new Int2ObjectOpenHashMap<>());
        var load = ChunkMap.class.getDeclaredMethod(
                "playerLoadedChunk", ServerPlayer.class, MutableObject.class, LevelChunk.class);
        load.setAccessible(true);
        var received = new ArrayList<ChunkWatchEvent.Watch>();
        Consumer<ChunkWatchEvent.Watch> observe = event -> {
            assertEquals(List.of(packet), packetsQueued);
            received.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            load.invoke(manager, watcher, new MutableObject<>(packet), chunk);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observe);
        }

        assertEquals(1, received.size());
        assertSame(watcher, received.get(0).getPlayer());
        assertSame(level, received.get(0).getLevel());
        assertSame(chunk, received.get(0).getChunk());
        assertEquals(pos, received.get(0).getPos());
    }
}
