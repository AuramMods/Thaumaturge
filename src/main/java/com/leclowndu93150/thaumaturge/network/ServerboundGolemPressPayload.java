package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.MenuGolemBuilder;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundGolemPressPayload(BlockPos pos, GolemProperties props, boolean craft)
        implements ModPayload {
    public static final Type<ServerboundGolemPressPayload> TYPE = new Type<>(TTIds.rl("golem_press"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundGolemPressPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ServerboundGolemPressPayload::pos,
                    GolemProperties.STREAM_CODEC,
                    ServerboundGolemPressPayload::props,
                    ByteBufCodecs.BOOL,
                    ServerboundGolemPressPayload::craft,
                    ServerboundGolemPressPayload::new);

    public static void handle(ServerboundGolemPressPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            BlockEntityGolemBuilder builder = BlockMenuGuard.target(context, payload.pos(), MenuGolemBuilder.class);
            if (builder == null) {
                return;
            }
            if (payload.craft()) {
                builder.beginAssembly(payload.props(), player);
            } else {
                boolean[] stuff = builder.stockedComponents(payload.props());
                byte[] bytes = new byte[stuff.length];
                for (int i = 0; i < stuff.length; i++) {
                    bytes[i] = (byte) (stuff[i] ? 1 : 0);
                }
                PayloadNetwork.sendToPlayer(player, new ClientboundGolemPressStuffPayload(payload.pos(), bytes));
            }
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
