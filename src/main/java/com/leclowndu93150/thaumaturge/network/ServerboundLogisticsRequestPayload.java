package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.logistics.MenuGolemLogistics;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundLogisticsRequestPayload(ItemStack stack, int amount) implements ModPayload {
    public static final Type<ServerboundLogisticsRequestPayload> TYPE = new Type<>(TTIds.rl("logistics_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLogisticsRequestPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.ITEM_STACK,
                    ServerboundLogisticsRequestPayload::stack,
                    ByteBufCodecs.VAR_INT,
                    ServerboundLogisticsRequestPayload::amount,
                    ServerboundLogisticsRequestPayload::new);

    public static void handle(ServerboundLogisticsRequestPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof MenuGolemLogistics menu) {
                menu.request(payload.stack(), payload.amount());
            }
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
