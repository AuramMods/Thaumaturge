package com.leclowndu93150.thaumaturge.network.transport;

import com.leclowndu93150.thaumaturge.datamap.ClientDataMapSync;
import com.leclowndu93150.thaumaturge.datamap.DataMapSync;
import com.leclowndu93150.thaumaturge.network.transport.PayloadCatalog.Flow;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Forge play transport for the mod's typed messages. */
public final class PayloadNetwork {
    private static final String PROTOCOL = "forge-1.20.1-3";
    private static final PayloadCatalog CATALOG = new PayloadCatalog();
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("thaumaturge", "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private record Clientbound(ModPayload payload) {}
    private record Serverbound(ModPayload payload) {}

    static {
        CHANNEL.messageBuilder(Clientbound.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((packet, buffer) -> CATALOG.encode(
                        new RegistryFriendlyByteBuf(buffer, serverRegistries()), packet.payload(), Flow.CLIENTBOUND))
                .decoder(buffer -> new Clientbound(CATALOG.decode(
                        new RegistryFriendlyByteBuf(buffer, clientboundRegistries()), Flow.CLIENTBOUND)))
                .consumerMainThread((packet, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> CATALOG.handle(packet.payload(), Flow.CLIENTBOUND, new ForgeContext(context.get()))))
                .add();
        CHANNEL.messageBuilder(Serverbound.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder((packet, buffer) -> CATALOG.encode(
                        new RegistryFriendlyByteBuf(buffer, ClientPayloadState.registries()), packet.payload(), Flow.SERVERBOUND))
                .decoder(buffer -> new Serverbound(CATALOG.decode(
                        new RegistryFriendlyByteBuf(buffer, serverRegistries()), Flow.SERVERBOUND)))
                .consumerMainThread((packet, context) ->
                        CATALOG.handle(packet.payload(), Flow.SERVERBOUND, new ForgeContext(context.get())))
                .add();
        CHANNEL.messageBuilder(DataMapSync.Fragment.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DataMapSync.Fragment::write)
                .decoder(DataMapSync.Fragment::read)
                .consumerMainThread((fragment, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientDataMapSync.receive(context.get().getNetworkManager(), fragment)))
                .add();
    }

    public static void sendDataMapFragment(Connection connection, DataMapSync.Fragment fragment) {
        CHANNEL.sendTo(fragment, connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private PayloadNetwork() {}

    public static Registrar registrar() {
        return new Registrar();
    }

    public static final class Registrar {
        private Registrar() {}

        public <T extends ModPayload> void playToClient(ModPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PayloadContext> handler) {
            CATALOG.register(type, codec, Flow.CLIENTBOUND, handler);
        }

        public <T extends ModPayload> void playToServer(ModPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PayloadContext> handler) {
            CATALOG.register(type, codec, Flow.SERVERBOUND, handler);
        }
    }

    public static void sendToServer(ModPayload... payloads) {
        for (ModPayload payload : payloads) {
            CHANNEL.sendToServer(new Serverbound(payload));
        }
    }

    public static void sendToPlayer(ServerPlayer player, ModPayload... payloads) {
        for (ModPayload payload : payloads) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Clientbound(payload));
        }
    }

    public static void sendToAllPlayers(ModPayload... payloads) {
        for (ModPayload payload : payloads) {
            CHANNEL.send(PacketDistributor.ALL.noArg(), new Clientbound(payload));
        }
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, ModPayload... payloads) {
        var target = PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity);
        for (ModPayload payload : payloads) {
            CHANNEL.send(target, new Clientbound(payload));
        }
    }

    public static void sendToPlayersNear(ServerLevel level, ServerPlayer excluded,
            double x, double y, double z, double radius, ModPayload... payloads) {
        var target = PacketDistributor.NEAR.with(
                () -> new PacketDistributor.TargetPoint(excluded, x, y, z, radius, level.dimension()));
        for (ModPayload payload : payloads) {
            CHANNEL.send(target, new Clientbound(payload));
        }
    }

    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos position, ModPayload... payloads) {
        // Query the actual tracking list without loading or generating the chunk.
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(position, false)) {
            sendToPlayer(player, payloads);
        }
    }

    private static RegistryAccess serverRegistries() {
        return Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(), "No running server for gameplay payload")
                .registryAccess();
    }

    private static HolderLookup.Provider clientboundRegistries() {
        // Forge decodes on the network thread before the client finishes installing its local player.
        HolderLookup.Provider received = RegistryContext.scoped();
        return received == null ? ClientPayloadState.registries() : received;
    }

    private record ForgeContext(NetworkEvent.Context context) implements PayloadContext {
        @Override
        public Player player() {
            return context.getDirection() == NetworkDirection.PLAY_TO_SERVER
                    ? Objects.requireNonNull(context.getSender(), "No sender for serverbound gameplay payload")
                    : ClientPayloadState.player();
        }

        @Override
        public CompletableFuture<Void> enqueueWork(Runnable work) {
            return context.enqueueWork(work);
        }
    }
}
