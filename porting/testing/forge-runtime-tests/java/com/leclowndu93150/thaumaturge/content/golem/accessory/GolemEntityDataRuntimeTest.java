package com.leclowndu93150.thaumaturge.content.golem.accessory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GolemEntityDataRuntimeTest {
    private static final ResourceKey<ChatType> SELECTED = ResourceKey.create(Registries.CHAT_TYPE, id("selected"));
    private static final ResourceKey<ChatType> OTHER = ResourceKey.create(Registries.CHAT_TYPE, id("other"));
    private static final StreamCodec<RegistryFriendlyByteBuf, Holder<ChatType>> CHAT_STREAM =
            ByteBufCodecs.holderRegistry(Registries.CHAT_TYPE);
    private static final GolemAccessoryBehavior<Holder<ChatType>> BEHAVIOR = new GolemAccessoryBehavior<>() {
        @Override
        public Codec<Holder<ChatType>> stateCodec() {
            return RegistryFixedCodec.create(Registries.CHAT_TYPE);
        }

        @Override
        public Holder<ChatType> initialState() {
            return Holder.direct(chat("initial"));
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Holder<ChatType>> syncCodec() {
            return StreamCodec.of((buffer, value) -> {
                assertSame(buffer.registryLookup(), RegistryContext.scoped());
                CHAT_STREAM.encode(buffer, value);
            }, buffer -> {
                assertSame(buffer.registryLookup(), RegistryContext.scoped());
                return CHAT_STREAM.decode(buffer);
            });
        }
    };
    private static final GolemAccessory ACCESSORY = new GolemAccessory(id("speaker"), GolemAccessory.Group.NONE,
            0, 1.0F, 1.0F, 1.0F, 0, false, Optional.of(BEHAVIOR));

    @BeforeAll
    static void registerSerializerAndAccessory() {
        GolemAccessories.register(ACCESSORY);
        ForgeRegistries.ENTITY_DATA_SERIALIZERS.get().register(id("accessory_states"), GolemAccessoryStates.ENTITY_DATA_SERIALIZER);
    }

    @Test
    void nativeEntityDataUsesTheSendingWorldAndReceivingLoginRegistries() {
        var server = lookup("server", true);
        var wrong = lookup("other_world", false);
        var holder = server.registryOrThrow(Registries.CHAT_TYPE).getHolderOrThrow(SELECTED);
        var value = new GolemAccessoryStates(List.of(new AccessoryStateSlot<>(ACCESSORY, BEHAVIOR, holder)));
        var serializer = GolemAccessoryStates.ENTITY_DATA_SERIALIZER;
        assertSame(value, serializer.copy(value));
        assertTrue(value.encodedSize(server) > 0);
        assertNull(RegistryContext.scoped());

        var level = mock(ServerLevel.class);
        var player = mock(ServerPlayer.class);
        var listener = mock(ServerGamePacketListenerImpl.class);
        when(level.registryAccess()).thenReturn(server);
        when(player.serverLevel()).thenReturn(level);
        when(listener.getPlayer()).thenReturn(player);
        var connection = new Connection(PacketFlow.SERVERBOUND);
        connection.setListener(listener);
        var encoder = new EmbeddedChannel(new PacketEncoder(PacketFlow.CLIENTBOUND), connection);
        encoder.attr(Connection.ATTRIBUTE_PROTOCOL).set(ConnectionProtocol.PLAY);
        var decoder = new EmbeddedChannel(new PacketDecoder(PacketFlow.CLIENTBOUND));
        decoder.attr(Connection.ATTRIBUTE_PROTOCOL).set(ConnectionProtocol.PLAY);
        try {
            var login = new ClientboundLoginPacket(1, false, GameType.SURVIVAL, null, Set.of(Level.OVERWORLD), server,
                    BuiltinDimensionTypes.OVERWORLD, Level.OVERWORLD, 0L, 2, 2, 2,
                    false, true, false, false, Optional.empty(), 0);
            ClientboundLoginPacket receivedLogin = send(encoder, decoder, login);
            var receiver = receivedLogin.registryHolder().registryOrThrow(Registries.CHAT_TYPE);
            var packet = new ClientboundSetEntityDataPacket(17, List.of(
                    DataValue.create(serializer.createAccessor(6), value),
                    DataValue.create(EntityDataSerializers.BYTE.createAccessor(7), (byte) 31)));

            ClientboundSetEntityDataPacket received = RegistryContext.withRegistries(wrong, () -> {
                ClientboundSetEntityDataPacket result = send(encoder, decoder, packet);
                assertSame(wrong, RegistryContext.scoped());
                return result;
            });
            assertEquals(17, received.id());
            assertEquals(2, received.packedItems().size());
            assertSame(serializer, received.packedItems().get(0).serializer());
            var state = (GolemAccessoryStates) received.packedItems().get(0).value();
            assertSame(receiver.getHolderOrThrow(SELECTED), state.state(BEHAVIOR).orElseThrow());
            assertNotSame(holder, state.state(BEHAVIOR).orElseThrow());
            assertEquals((byte) 31, received.packedItems().get(1).value());
            assertNull(RegistryContext.scoped());
        } finally {
            decoder.finishAndReleaseAll();
            encoder.finishAndReleaseAll();
        }
    }

    @Test
    void slotCountMustFitTheAvailablePacketData() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeVarInt(-1);
            assertThrows(DecoderException.class, () -> GolemAccessoryStates.ENTITY_DATA_SERIALIZER.read(buffer));
            buffer.clear();
            buffer.writeVarInt(30);
            buffer.writeByte(0);
            assertThrows(DecoderException.class, () -> GolemAccessoryStates.ENTITY_DATA_SERIALIZER.read(buffer));
        } finally {
            buffer.release();
        }
    }

    private static <P extends Packet<?>> P send(EmbeddedChannel encoder, EmbeddedChannel decoder, P packet) {
        assertTrue(encoder.writeOutbound(packet));
        ByteBuf wire = encoder.readOutbound();
        assertTrue(decoder.writeInbound(wire));
        return decoder.readInbound();
    }

    private static RegistryAccess.Frozen lookup(String prefix, boolean reversed) {
        var registry = new MappedRegistry<ChatType>(Registries.CHAT_TYPE, Lifecycle.stable());
        if (reversed) {
            registry.register(OTHER, chat(prefix + "_other"), Lifecycle.stable());
        }
        registry.register(SELECTED, chat(prefix + "_selected"), Lifecycle.stable());
        if (!reversed) {
            registry.register(OTHER, chat(prefix + "_other"), Lifecycle.stable());
        }
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
    }

    private static ChatType chat(String key) {
        return new ChatType(ChatTypeDecoration.withSender(key), ChatTypeDecoration.withSender(key));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("port_golem_sync", path);
    }
}
