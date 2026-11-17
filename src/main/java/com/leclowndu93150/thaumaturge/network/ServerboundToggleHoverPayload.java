package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverManager;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundToggleHoverPayload() implements ModPayload {
    public static final ServerboundToggleHoverPayload INSTANCE = new ServerboundToggleHoverPayload();

    public static final Type<ServerboundToggleHoverPayload> TYPE = new Type<>(TTIds.rl("toggle_hover"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundToggleHoverPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public static void handle(ServerboundToggleHoverPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                HoverManager.toggle(player);
            }
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
