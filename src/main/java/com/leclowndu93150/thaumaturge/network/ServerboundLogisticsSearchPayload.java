package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.logistics.MenuGolemLogistics;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundLogisticsSearchPayload(String text) implements ModPayload {
    public static final Type<ServerboundLogisticsSearchPayload> TYPE = new Type<>(TTIds.rl("logistics_search"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLogisticsSearchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.stringUtf8(MenuGolemLogistics.SEARCH_MAX_LENGTH),
                    ServerboundLogisticsSearchPayload::text,
                    ServerboundLogisticsSearchPayload::new);

    public static void handle(ServerboundLogisticsSearchPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof MenuGolemLogistics menu) {
                menu.setSearchText(payload.text());
            }
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
