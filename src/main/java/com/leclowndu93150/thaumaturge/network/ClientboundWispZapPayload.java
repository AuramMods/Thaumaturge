package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundWispZapPayload(int sourceId, int targetId) implements CustomPacketPayload {
    public static final Type<ClientboundWispZapPayload> TYPE = new Type<>(TTIds.rl("wisp_zap"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWispZapPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundWispZapPayload::sourceId,
                    ByteBufCodecs.VAR_INT,
                    ClientboundWispZapPayload::targetId,
                    ClientboundWispZapPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
