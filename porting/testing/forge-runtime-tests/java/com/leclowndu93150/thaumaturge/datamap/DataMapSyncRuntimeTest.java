package com.leclowndu93150.thaumaturge.datamap;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import org.junit.jupiter.api.Test;

class DataMapSyncRuntimeTest {
    private static final ResourceKey<Registry<String>> ROOT = root("entries");
    private static final ResourceKey<Registry<String>> TARGET = root("values");
    private static final ResourceKey<String> ENTRY = ResourceKey.create(ROOT, id("first"));
    private static final ResourceKey<String> VALUE = ResourceKey.create(TARGET, id("value"));
    private static final DataMapType<String, Holder<String>> HOLDERS =
            DataMapType.builder(id("holders"), ROOT, RegistryFixedCodec.create(TARGET))
                    .synced(RegistryFixedCodec.create(TARGET), false).build();
    private static final DataMapType<String, Integer> LOCAL = DataMapType.builder(id("local"), ROOT, Codec.INT).build();
    private static final DataMapType<String, Integer> COUNT = DataMapType.builder(id("count"), ROOT, Codec.INT)
            .synced(Codec.INT, false).build();
    private static final DataMapType<String, Integer> REQUIRED = DataMapType.builder(id("required"), ROOT, Codec.INT)
            .synced(Codec.INT, true).build();

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("port_sync", path);
    }

    private static ResourceKey<Registry<String>> root(String path) {
        return ResourceKey.createRegistryKey(id(path));
    }

    private RegistryAccess.Frozen owner(String name) {
        var entries = new MappedRegistry<String>(ROOT, Lifecycle.stable());
        entries.register(ENTRY, name + " entry", Lifecycle.stable());
        var values = new MappedRegistry<String>(TARGET, Lifecycle.stable());
        values.register(VALUE, name + " value", Lifecycle.stable());
        return new RegistryAccess.ImmutableRegistryAccess(List.of(entries.freeze(), values.freeze())).freeze();
    }

    private DataMapCatalog catalog(DataMapType<?, ?>... types) {
        var catalog = new DataMapCatalog();
        for (var type : types) catalog.register(type);
        catalog.freeze();
        return catalog;
    }

    private void receive(DataMapSync.Receiver receiver, Connection connection, List<DataMapSync.Fragment> fragments) {
        for (var fragment : fragments) receiver.receive(connection, fragment);
    }

    @Test
    void tagsCommitTheBatchAndValuesUseTheReceivingRegistryOwners() {
        var server = owner("server");
        var client = owner("client");
        var serverEntry = server.registryOrThrow(ROOT).getHolderOrThrow(ENTRY);
        var clientEntry = client.registryOrThrow(ROOT).getHolderOrThrow(ENTRY);
        var clientValue = client.registryOrThrow(TARGET).getHolderOrThrow(VALUE);
        var serverValue = server.registryOrThrow(TARGET).getHolderOrThrow(VALUE);
        var catalog = catalog(HOLDERS, COUNT, LOCAL);
        DataMaps.replace(server.registryOrThrow(ROOT), Map.of(
                HOLDERS, Map.of(ENTRY, serverValue), COUNT, Map.of(ENTRY, 42), LOCAL, Map.of(ENTRY, 12)));
        var receiver = new DataMapSync.Receiver(catalog);
        var connection = new Connection(PacketFlow.CLIENTBOUND);
        var notifications = new AtomicInteger();
        Consumer<DataMapsUpdatedEvent> listener = event -> {
            assertEquals(DataMapsUpdatedEvent.UpdateCause.CLIENT_SYNC, event.getCause());
            assertSame(client, event.getRegistries());
            assertEquals(ROOT, event.getRegistryKey());
            assertEquals(42, DataMaps.get(clientEntry, COUNT));
            assertSame(clientValue, DataMaps.get(clientEntry, HOLDERS));
            notifications.incrementAndGet();
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            receive(receiver, connection, DataMapSync.snapshot(server, catalog));
            assertNull(DataMaps.get(clientEntry, HOLDERS));
            assertEquals(0, notifications.get());
            receiver.tagsApplied(new Connection(PacketFlow.CLIENTBOUND), client);
            assertEquals(0, notifications.get());
            receiver.tagsApplied(connection, client);
            assertEquals(1, notifications.get());
            assertNull(DataMaps.get(clientEntry, LOCAL));
            assertEquals(12, DataMaps.get(serverEntry, LOCAL));
            assertSame(serverValue, DataMaps.get(serverEntry, HOLDERS));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }

        DataMaps.replace(server.registryOrThrow(ROOT), Map.of());
        receive(receiver, connection, DataMapSync.snapshot(server, catalog));
        assertEquals(42, DataMaps.get(clientEntry, COUNT));
        receiver.tagsApplied(connection, client);
        assertNull(DataMaps.get(clientEntry, COUNT));
        assertTrue(DataMaps.loaded(clientEntry, COUNT));
        receiver.clear(connection);
        assertFalse(DataMaps.loaded(clientEntry, COUNT));
    }

    @Test
    void optionalUnknownMapsAreSkippedAndRequiredUnknownMapsRejectTheEntireBatch() {
        var server = owner("server");
        var client = owner("client");
        var clientEntry = client.registryOrThrow(ROOT).getHolderOrThrow(ENTRY);
        var connection = new Connection(PacketFlow.CLIENTBOUND);
        var receiver = new DataMapSync.Receiver(catalog(COUNT));
        DataMaps.replace(server.registryOrThrow(ROOT), Map.of(COUNT, Map.of(ENTRY, 42), REQUIRED, Map.of(ENTRY, 1)));
        receive(receiver, connection, DataMapSync.snapshot(server, catalog(COUNT, HOLDERS)));
        receiver.tagsApplied(connection, client);
        assertEquals(42, DataMaps.get(clientEntry, COUNT));

        DataMaps.replace(server.registryOrThrow(ROOT), Map.of(COUNT, Map.of(ENTRY, 99), REQUIRED, Map.of(ENTRY, 1)));
        receive(receiver, connection, DataMapSync.snapshot(server, catalog(COUNT, REQUIRED)));
        var error = assertThrows(DecoderException.class, () -> receiver.tagsApplied(connection, client));
        assertTrue(error.getMessage().contains("Unsupported required data map"));
        assertEquals(42, DataMaps.get(clientEntry, COUNT));
    }

    @Test
    void switchingConnectionsClearsOldOwnersAndIgnoresStaleLogout() {
        var server = owner("server");
        var first = owner("first");
        var second = owner("second");
        var clientEntry = first.registryOrThrow(ROOT).getHolderOrThrow(ENTRY);
        var secondEntry = second.registryOrThrow(ROOT).getHolderOrThrow(ENTRY);
        var catalog = catalog(COUNT);
        DataMaps.replace(server.registryOrThrow(ROOT), Map.of(COUNT, Map.of(ENTRY, 42)));
        var fragments = DataMapSync.snapshot(server, catalog);
        var receiver = new DataMapSync.Receiver(catalog);
        var firstConnection = new Connection(PacketFlow.CLIENTBOUND);
        var secondConnection = new Connection(PacketFlow.CLIENTBOUND);

        receive(receiver, firstConnection, fragments);
        receiver.tagsApplied(firstConnection, first);
        assertEquals(42, DataMaps.get(clientEntry, COUNT));
        receive(receiver, secondConnection, fragments);
        assertNull(DataMaps.get(clientEntry, COUNT));
        receiver.clear(firstConnection);
        receiver.tagsApplied(secondConnection, second);
        assertEquals(42, DataMaps.get(secondEntry, COUNT));
        receiver.clear(secondConnection);
        assertNull(DataMaps.get(secondEntry, COUNT));
    }

    @Test
    void fragmentsCrossActualForgeCustomPacketFramingAndRequireAllPartsBeforeTags() throws Exception {
        var server = new MappedRegistry<String>(ROOT, Lifecycle.stable());
        var client = new MappedRegistry<String>(ROOT, Lifecycle.stable());
        var type = DataMapType.builder(id("large"), ROOT, Codec.STRING).synced(Codec.STRING, false).build();
        var values = new LinkedHashMap<ResourceKey<String>, String>();
        for (int index = 0; index < 40; ++index) {
            var key = ResourceKey.create(ROOT, id("entry_" + index));
            server.register(key, "server " + index, Lifecycle.stable());
            client.register(key, "client " + index, Lifecycle.stable());
            values.put(key, "x".repeat(30_000) + index);
        }
        var serverOwner = new RegistryAccess.ImmutableRegistryAccess(List.of(server.freeze())).freeze();
        var clientOwner = new RegistryAccess.ImmutableRegistryAccess(List.of(client.freeze())).freeze();
        DataMaps.replace(server, Map.of(type, values));
        var catalog = catalog(type);
        var fragments = DataMapSync.snapshot(serverOwner, catalog);
        assertEquals(3, fragments.size());

        // Use the registered Forge message and vanilla custom-payload framing on both distributions.
        var channelField = PayloadNetwork.class.getDeclaredField("CHANNEL");
        channelField.setAccessible(true);
        var channel = (SimpleChannel) channelField.get(null);
        var receiver = new DataMapSync.Receiver(catalog);
        var connection = new Connection(PacketFlow.CLIENTBOUND);
        assertThrows(DecoderException.class, () -> receiver.receive(connection, fragments.get(1)));
        for (var fragment : fragments) {
            var packet = (ClientboundCustomPayloadPacket) channel.toVanillaPacket(fragment, NetworkDirection.PLAY_TO_CLIENT);
            var wire = new FriendlyByteBuf(Unpooled.buffer());
            try {
                packet.write(wire);
                var decoded = new ClientboundCustomPayloadPacket(wire);
                assertEquals(ResourceLocation.fromNamespaceAndPath("thaumaturge", "main"), decoded.getIdentifier());
                assertFalse(wire.isReadable());
                var data = decoded.getInternalData();
                try {
                    assertEquals(2, data.readUnsignedByte());
                    receiver.receive(connection, DataMapSync.Fragment.read(data));
                } finally {
                    data.release();
                }
            } finally {
                wire.release();
                packet.getInternalData().release();
            }
            if (!fragment.last()) assertThrows(DecoderException.class, () -> receiver.tagsApplied(connection, clientOwner));
        }
        receiver.tagsApplied(connection, clientOwner);
        values.forEach((key, value) -> assertEquals(value, DataMaps.get(client.getHolderOrThrow(key), type)));
    }
}
