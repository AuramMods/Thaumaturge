package com.leclowndu93150.thaumaturge.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverFlight;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class KnownMovementRuntimeTest {
    private static final Vec3 START = new Vec3(4, 64, -3);
    private static final Vec3 STEP = new Vec3(0.25, 0.125, -0.5);

    @Test
    void nativePlayerHandlerRecordsAcceptedMovementAndStationaryPackets() throws Exception {
        var player = mock(ServerPlayer.class);
        bindPosition(player, START);
        var listener = listener(player);
        assertEquals(Vec3.ZERO, KnownMovement.of(player));

        Vec3 destination = START.add(STEP);
        var packet = new ServerboundMovePlayerPacket.Pos(destination.x, destination.y, destination.z, true);
        listener.handleMovePlayer(packet);
        assertEquals(destination, player.position());
        assertEquals(STEP, KnownMovement.of(player));

        listener.handleMovePlayer(packet);
        assertEquals(Vec3.ZERO, KnownMovement.of(player));
    }

    @Test
    void nativeVehicleHandlerRecordsTheVehiclesMovementForBothDriverAndPassenger() throws Exception {
        var vehicle = mock(Entity.class);
        bindPosition(vehicle, START);
        when(vehicle.isAlive()).thenReturn(true);
        var driver = mock(ServerPlayer.class);
        bindPosition(driver, START.add(0, 1.25, 0));
        when(driver.getVehicle()).thenReturn(vehicle);
        when(driver.getRootVehicle()).thenReturn(vehicle);
        when(vehicle.getControllingPassenger()).thenReturn(driver);
        var listener = listener(driver);
        setField(listener, "lastVehicle", vehicle);
        setVehiclePosition(listener, "vehicleFirstGood");
        setVehiclePosition(listener, "vehicleLastGood");

        Vec3 destination = START.add(STEP);
        var clientVehicle = mock(Entity.class);
        bindPosition(clientVehicle, destination);
        listener.handleMoveVehicle(new ServerboundMoveVehiclePacket(clientVehicle));

        var passenger = mock(ServerPlayer.class);
        when(passenger.getVehicle()).thenReturn(vehicle);
        assertEquals(destination, vehicle.position());
        assertEquals(STEP, KnownMovement.of(driver));
        assertEquals(STEP, KnownMovement.of(vehicle));
        assertEquals(STEP, KnownMovement.of(passenger));

        when(vehicle.getControllingPassenger()).thenReturn(null);
        when(vehicle.getDeltaMovement()).thenReturn(new Vec3(0, -0.1, 0));
        assertEquals(vehicle.getDeltaMovement(), KnownMovement.of(vehicle));
        assertEquals(vehicle.getDeltaMovement(), KnownMovement.of(passenger));
    }

    @Test
    void hoverSuppressesNativeFloatingDetectionWithoutChangingBaseAbilities() throws Exception {
        var player = mock(ServerPlayer.class);
        bindPosition(player, START);
        var listener = listener(player);
        when(player.serverLevel().getServer().isFlightAllowed()).thenReturn(false);
        var abilities = new Abilities();
        when(player.getAbilities()).thenReturn(abilities);
        var hover = (HoverFlight) player;
        doCallRealMethod().when(hover).thaumaturge$setHoverFlight(anyBoolean());
        doCallRealMethod().when(hover).thaumaturge$hasHoverFlight();
        var floating = ServerGamePacketListenerImpl.class.getDeclaredField("clientIsFloating");
        floating.setAccessible(true);
        var packet = new ServerboundMovePlayerPacket.Pos(START.x, START.y, START.z, false);

        listener.handleMovePlayer(packet);
        assertTrue(floating.getBoolean(listener));

        hover.thaumaturge$setHoverFlight(true);
        listener.handleMovePlayer(packet);
        assertFalse(floating.getBoolean(listener));
        assertFalse(abilities.mayfly);

        hover.thaumaturge$setHoverFlight(false);
        listener.handleMovePlayer(packet);
        assertTrue(floating.getBoolean(listener));
    }

    private static ServerGamePacketListenerImpl listener(ServerPlayer player) throws Exception {
        var server = mock(MinecraftServer.class);
        when(server.isSameThread()).thenReturn(true);
        when(server.isFlightAllowed()).thenReturn(true);
        var world = mock(ServerLevel.class);
        var chunks = mock(ServerChunkCache.class);
        when(world.getServer()).thenReturn(server);
        when(world.getChunkSource()).thenReturn(chunks);
        when(world.getGameRules()).thenReturn(new GameRules());
        when(world.noCollision(any(Entity.class), any(AABB.class))).thenReturn(true);
        when(world.getBlockCollisions(any(Entity.class), any(AABB.class))).thenReturn(List.of());

        var mode = mock(ServerPlayerGameMode.class);
        when(mode.getGameModeForPlayer()).thenReturn(GameType.SURVIVAL);
        var textFilter = mock(TextFilter.class);
        when(player.getTextFilter()).thenReturn(textFilter);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.serverLevel()).thenReturn(world);
        when(player.level()).thenReturn(world);
        setField(player, "gameMode", mode);

        var access = (KnownMovement.Access) player;
        doCallRealMethod().when(access).thaumaturge$setKnownMovement(any(Vec3.class));
        doCallRealMethod().when(access).thaumaturge$getKnownMovement();
        // The fixture uses a mocked player, so the real mixin field initializer has not run.
        access.thaumaturge$setKnownMovement(Vec3.ZERO);
        var listener = new ServerGamePacketListenerImpl(server, mock(Connection.class), player);
        listener.resetPosition();
        return listener;
    }

    private static void bindPosition(Entity entity, Vec3 start) {
        var position = new AtomicReference<>(start);
        when(entity.position()).thenAnswer(call -> position.get());
        when(entity.getX()).thenAnswer(call -> position.get().x);
        when(entity.getY()).thenAnswer(call -> position.get().y);
        when(entity.getZ()).thenAnswer(call -> position.get().z);
        when(entity.getDeltaMovement()).thenReturn(Vec3.ZERO);
        when(entity.getBoundingBox()).thenAnswer(call -> new AABB(0, 0, 0, 1, 1, 1).move(position.get()));
        doAnswer(call -> {
            position.updateAndGet(pos -> pos.add(call.getArgument(1, Vec3.class)));
            return null;
        }).when(entity).move(any(MoverType.class), any(Vec3.class));
        doAnswer(call -> {
            position.set(new Vec3(call.getArgument(0), call.getArgument(1), call.getArgument(2)));
            return null;
        }).when(entity).absMoveTo(anyDouble(), anyDouble(), anyDouble(), anyFloat(), anyFloat());
    }

    private static void setVehiclePosition(ServerGamePacketListenerImpl listener, String fieldPrefix) throws Exception {
        setField(listener, fieldPrefix + "X", START.x);
        setField(listener, fieldPrefix + "Y", START.y);
        setField(listener, fieldPrefix + "Z", START.z);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Class<?> owner = target instanceof ServerPlayer ? ServerPlayer.class : ServerGamePacketListenerImpl.class;
        var field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
