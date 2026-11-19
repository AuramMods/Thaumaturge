package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTTicketTypes;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LabyrinthPortalRuntimeTest {
    @Test
    void warmupCountsOneContactPerTickDuringPreloadAndDecaysOutsideThePortal() {
        var level = mock(ServerLevel.class);
        var state = new TransitState();
        for (int tick = 0; tick < 5; tick++) {
            state.markContact(level);
            state.markContact(level);
            assertFalse(state.tickPortal(level, true, Integer.MAX_VALUE));
        }
        assertFalse(state.tickPortal(level, true, 2));
        state.markContact(level);
        assertFalse(state.tickPortal(level, true, 2));
        state.markContact(level);
        assertTrue(state.tickPortal(level, true, 2));
    }

    @Test
    void contactCannotTransferBetweenDimensionsAndRidingPlayersDoNotAdvanceTheTimer() {
        var source = mock(ServerLevel.class);
        var destination = mock(ServerLevel.class);
        var state = new TransitState();
        state.markContact(source);
        assertFalse(state.tickPortal(destination, true, 0));
        state.markContact(destination);
        assertFalse(state.tickPortal(destination, false, 1));
        state.markContact(destination);
        assertFalse(state.tickPortal(destination, true, 1));
        state.markContact(destination);
        assertTrue(state.tickPortal(destination, true, 1));
    }

    @Test
    void releasingOnePlayersTicketDoesNotRemoveAnotherPlayersTicketInTheSameChunk() {
        var type = TTTicketTypes.LABYRINTH_TRANSIT;
        var first = new Ticket<>(type, 32, new UUID(0, 1), false);
        var second = new Ticket<>(type, 32, new UUID(0, 2), false);
        SortedArraySet<Ticket<?>> tickets = SortedArraySet.create(2);
        assertTrue(tickets.add(first));
        assertTrue(tickets.add(second));
        assertTrue(tickets.remove(first));
        assertEquals(1, tickets.size());
        assertTrue(tickets.contains(second));
    }

    @Test
    void sameDimensionTravelUsesTheNativeConnectionAndPlaysThePortalSoundForTheTravellingPlayer() {
        var level = mock(ServerLevel.class);
        var player = mock(ServerPlayer.class);
        player.connection = mock(ServerGamePacketListenerImpl.class);
        when(player.serverLevel()).thenReturn(level);
        var destination = new LabyrinthDestination(level, new Vec3(4.5, 70, -8.5), 45.0F, 4.0F);

        assertTrue(destination.teleport(player, true));

        verify(player.connection).teleport(4.5, 70, -8.5, 45.0F, 4.0F);
        verify(player.connection).resetPosition();
        var sound = ArgumentCaptor.forClass(ClientboundLevelEventPacket.class);
        verify(player.connection).send(sound.capture());
        assertEquals(LevelEvent.SOUND_PORTAL_TRAVEL, sound.getValue().getType());
        verify(player, never()).changeDimension(any(ServerLevel.class), any(ITeleporter.class));
    }

    @Test
    void nativeCrossDimensionTravelUsesTheAuthoredArrivalAndDoesNotBuildAnEndPlatform() throws Exception {
        var source = level(Level.OVERWORLD);
        var destination = level(Level.END);
        var server = mock(MinecraftServer.class);
        var players = mock(PlayerList.class);
        when(server.getPlayerList()).thenReturn(players);
        var player = playerAt(server, source);
        var arrival = new LabyrinthDestination(destination, new Vec3(20.5, 90, -12.5), 30.0F, -15.0F);

        assertTrue(arrival.teleport(player, false));

        verify(source).removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
        verify(destination).addDuringPortalTeleport(player);
        assertEquals(destination, player.serverLevel());
        verify(player.connection).teleport(20.5, 90, -12.5, 30.0F, -15.0F);
        verify(player.connection).resetPosition();
        verify(destination, never()).setBlockAndUpdate(any(), any());
        verify(player.connection, never()).send(any(ClientboundLevelEventPacket.class));
    }

    @Test
    void forgeTravelVetoKeepsThePlayerInTheSourceDimension() throws Exception {
        var source = level(Level.OVERWORLD);
        var destination = level(Level.END);
        var player = playerAt(mock(MinecraftServer.class), source);
        var arrival = new LabyrinthDestination(destination, Vec3.ZERO, 0.0F, 0.0F);
        Consumer<EntityTravelToDimensionEvent> veto = event -> {
            if (event.getEntity() == player) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            assertFalse(arrival.teleport(player, true));
            assertEquals(source, player.serverLevel());
            verify(source, never()).removePlayerImmediately(eq(player), any());
            verify(destination, never()).addDuringPortalTeleport(player);
            verify(player.connection, never()).send(any());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
    }

    @Test
    void returnFallsBackToTheStoredSpawnWithItsAngleWhenNoAltarLandingIsLoaded() {
        var source = level(Level.OVERWORLD);
        var respawnLevel = level(Level.NETHER);
        var server = mock(MinecraftServer.class);
        when(source.getServer()).thenReturn(server);
        when(server.getLevel(Level.NETHER)).thenReturn(respawnLevel);
        var player = mock(ServerPlayer.class);
        var respawnPos = new BlockPos(8, 72, -2);
        when(player.serverLevel()).thenReturn(source);
        when(player.getRespawnPosition()).thenReturn(respawnPos);
        when(player.getRespawnDimension()).thenReturn(Level.NETHER);
        when(player.isRespawnForced()).thenReturn(true);
        when(player.getRespawnAngle()).thenReturn(65.0F);
        when(respawnLevel.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());

        var result = ReturnPointFinder.find(player, source, BlockPos.ZERO, 0);

        assertEquals(new LabyrinthDestination(respawnLevel, new Vec3(8.5, 72.1, -1.5), 65.0F, 0.0F), result);
    }

    @Test
    void blockedRespawnFallsBackToAdjustedWorldSpawnAndSendsTheMissingSpawnNotice() {
        var overworld = level(Level.OVERWORLD);
        var server = mock(MinecraftServer.class);
        var dimension = mock(DimensionType.class);
        when(overworld.getServer()).thenReturn(server);
        when(overworld.dimensionType()).thenReturn(dimension);
        when(overworld.getSharedSpawnPos()).thenReturn(new BlockPos(7, 64, 8));
        when(overworld.getMaxBuildHeight()).thenReturn(320);
        when(overworld.getMinBuildHeight()).thenReturn(-64);
        when(overworld.getBlockState(any(BlockPos.class))).thenReturn(Blocks.STONE.defaultBlockState());
        when(server.overworld()).thenReturn(overworld);
        when(server.getLevel(Level.OVERWORLD)).thenReturn(overworld);
        var player = mock(ServerPlayer.class);
        player.connection = mock(ServerGamePacketListenerImpl.class);
        var standing = EntityType.PLAYER.getDimensions();
        when(player.getDimensions(Pose.STANDING)).thenReturn(standing);
        when(player.serverLevel()).thenReturn(overworld);
        when(player.getRespawnPosition()).thenReturn(new BlockPos(-8, 64, -8));
        when(player.getRespawnDimension()).thenReturn(Level.OVERWORLD);
        when(overworld.noCollision(eq(player), any(AABB.class)))
                .thenAnswer(call -> call.getArgument(1, AABB.class).minY >= 65);

        var result = ReturnPointFinder.find(player, overworld, BlockPos.ZERO, 0);
        assertEquals(new LabyrinthDestination(overworld, new Vec3(7.5, 65, 8.5), 0.0F, 0.0F, true), result);
        assertTrue(result.teleport(player, false));

        var notice = ArgumentCaptor.forClass(ClientboundGameEventPacket.class);
        verify(player.connection).send(notice.capture());
        assertEquals(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, notice.getValue().getEvent());
    }

    @Test
    void worldSpawnWithSkylightUsesTheNativeSurfaceSearch() {
        var overworld = level(Level.OVERWORLD);
        var server = mock(MinecraftServer.class);
        var dimension = mock(DimensionType.class);
        var worldData = mock(WorldData.class);
        var chunk = mock(LevelChunk.class);
        when(overworld.getServer()).thenReturn(server);
        when(overworld.dimensionType()).thenReturn(dimension);
        when(dimension.hasSkyLight()).thenReturn(true);
        when(server.getWorldData()).thenReturn(worldData);
        when(worldData.getGameType()).thenReturn(GameType.SURVIVAL);
        when(server.overworld()).thenReturn(overworld);
        when(overworld.getSharedSpawnPos()).thenReturn(new BlockPos(7, 64, 8));
        when(overworld.getWorldBorder()).thenReturn(new WorldBorder());
        when(overworld.getChunk(0, 0)).thenReturn(chunk);
        when(chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, 7, 8)).thenReturn(67);
        when(overworld.getBlockState(any(BlockPos.class))).thenAnswer(call ->
                call.getArgument(0, BlockPos.class).getY() == 67 ? Blocks.STONE.defaultBlockState()
                        : Blocks.AIR.defaultBlockState());
        var player = mock(ServerPlayer.class);
        var standing = EntityType.PLAYER.getDimensions();
        when(player.getDimensions(Pose.STANDING)).thenReturn(standing);
        when(player.serverLevel()).thenReturn(overworld);
        when(player.getRespawnDimension()).thenReturn(Level.OVERWORLD);
        when(overworld.noCollision(eq(player), any(AABB.class))).thenReturn(true);

        var result = ReturnPointFinder.find(player, overworld, BlockPos.ZERO, 0);

        assertEquals(new LabyrinthDestination(overworld, new Vec3(7.5, 68, 8.5), 0.0F, 0.0F), result);
        verify(overworld).getChunk(0, 0);
    }

    private static ServerLevel level(ResourceKey<Level> key) {
        var level = mock(ServerLevel.class);
        var data = mock(LevelData.class);
        when(data.getDifficulty()).thenReturn(Difficulty.NORMAL);
        when(level.getLevelData()).thenReturn(data);
        when(level.dimension()).thenReturn(key);
        when(level.getProfiler()).thenReturn(InactiveProfiler.INSTANCE);
        return level;
    }

    private static ServerPlayer playerAt(MinecraftServer server, ServerLevel level) throws Exception {
        var player = mock(ServerPlayer.class);
        player.connection = mock(ServerGamePacketListenerImpl.class);
        var currentLevel = new AtomicReference<>(level);
        when(player.serverLevel()).thenAnswer(call -> currentLevel.get());
        when(player.level()).thenAnswer(call -> currentLevel.get());
        doAnswer(call -> {
            currentLevel.set(call.getArgument(0, ServerLevel.class));
            return null;
        }).when(player).setServerLevel(any(ServerLevel.class));
        when(player.getAbilities()).thenReturn(new Abilities());
        var advancements = mock(PlayerAdvancements.class);
        when(player.getAdvancements()).thenReturn(advancements);
        var mode = mock(ServerPlayerGameMode.class);
        when(mode.getGameModeForPlayer()).thenReturn(GameType.SURVIVAL);
        for (var entry : java.util.Map.of("server", server, "gameMode", mode).entrySet()) {
            var field = ServerPlayer.class.getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(player, entry.getValue());
        }
        doCallRealMethod().when(player).changeDimension(any(ServerLevel.class), any(ITeleporter.class));
        return player;
    }
}
