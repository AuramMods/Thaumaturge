package com.leclowndu93150.thaumaturge.content.equipment.hover;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import org.junit.jupiter.api.Test;

class HoverFlightRuntimeTest {
    @Test
    void serverFlightToggleHonorsHoverAndKeepsIndependentPermission() {
        var player = player();
        var level = mock(ServerLevel.class);
        var server = mock(MinecraftServer.class);
        when(server.isSameThread()).thenReturn(true);
        when(player.serverLevel()).thenReturn(level);
        when(level.getServer()).thenReturn(server);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.getTextFilter()).thenReturn(mock(TextFilter.class));
        var connection = new ServerGamePacketListenerImpl(server, mock(Connection.class), player);
        var abilities = player.getAbilities();
        var requested = new Abilities();
        requested.flying = true;
        var startFlying = new ServerboundPlayerAbilitiesPacket(requested);

        connection.handlePlayerAbilities(startFlying);
        assertFalse(abilities.flying);

        ((HoverFlight) player).thaumaturge$setHoverFlight(true);
        connection.handlePlayerAbilities(startFlying);
        assertTrue(abilities.flying);
        assertFalse(abilities.mayfly);
        connection.handlePlayerAbilities(new ServerboundPlayerAbilitiesPacket(new Abilities()));
        assertFalse(abilities.flying);

        ((HoverFlight) player).thaumaturge$setHoverFlight(false);
        connection.handlePlayerAbilities(startFlying);
        assertFalse(abilities.flying);
        abilities.mayfly = true;
        connection.handlePlayerAbilities(startFlying);
        assertTrue(abilities.flying);
    }

    @Test
    void nativeAbilityPacketsSendTheTemporaryGrantAndPreserveTheOtherFields() {
        var player = player();
        var connection = mock(ServerGamePacketListenerImpl.class);
        player.connection = connection;
        doCallRealMethod().when(player).onUpdateAbilities();
        var abilities = player.getAbilities();
        abilities.flying = true;
        abilities.invulnerable = true;
        abilities.instabuild = true;
        abilities.setFlyingSpeed(0.075F);
        abilities.setWalkingSpeed(0.13F);
        var packets = new ArrayList<Packet<?>>();
        doAnswer(call -> { packets.add(call.getArgument(0)); return null; }).when(connection).send(any());

        ((HoverFlight) player).thaumaturge$setHoverFlight(true);
        player.onUpdateAbilities();
        var added = roundTrip(assertInstanceOf(ClientboundPlayerAbilitiesPacket.class, packets.get(0)));
        assertTrue(added.canFly());
        assertTrue(added.isFlying());
        assertTrue(added.isInvulnerable());
        assertTrue(added.canInstabuild());
        assertEquals(0.075F, added.getFlyingSpeed());
        assertEquals(0.13F, added.getWalkingSpeed());
        assertFalse(abilities.mayfly);

        ((HoverFlight) player).thaumaturge$setHoverFlight(false);
        abilities.flying = false;
        player.onUpdateAbilities();
        var removed = roundTrip(assertInstanceOf(ClientboundPlayerAbilitiesPacket.class, packets.get(1)));
        assertFalse(removed.canFly());
        assertFalse(removed.isFlying());
        abilities.mayfly = true;
        player.onUpdateAbilities();
        assertTrue(assertInstanceOf(ClientboundPlayerAbilitiesPacket.class, packets.get(2)).canFly());
    }

    @Test
    void hoverUsesTheNativeForgeFlyableFallPath() throws ReflectiveOperationException {
        var player = player();
        var field = Player.class.getDeclaredField("abilities");
        field.setAccessible(true);
        field.set(player, player.getAbilities());
        when(player.causeFallDamage(anyFloat(), anyFloat(), any(DamageSource.class))).thenCallRealMethod();
        var observed = new ArrayList<PlayerFlyableFallEvent>();
        Consumer<PlayerFlyableFallEvent> listener = event -> {
            if (event.getEntity() == player) observed.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            ((HoverFlight) player).thaumaturge$setHoverFlight(true);
            assertFalse(player.causeFallDamage(15.0F, 1.0F, mock(DamageSource.class)));
            assertEquals(1, observed.size());
            assertFalse(player.getAbilities().mayfly);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    private static ServerPlayer player() {
        var player = mock(ServerPlayer.class);
        var abilities = new Abilities();
        when(player.getAbilities()).thenReturn(abilities);
        var hover = (HoverFlight) player;
        doCallRealMethod().when(hover).thaumaturge$setHoverFlight(anyBoolean());
        doCallRealMethod().when(hover).thaumaturge$hasHoverFlight();
        return player;
    }

    private static ClientboundPlayerAbilitiesPacket roundTrip(ClientboundPlayerAbilitiesPacket packet) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.write(buffer);
            var decoded = new ClientboundPlayerAbilitiesPacket(buffer);
            assertFalse(buffer.isReadable());
            return decoded;
        } finally {
            buffer.release();
        }
    }
}
