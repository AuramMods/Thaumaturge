package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundWarpFXPayload(byte kind) implements ModPayload {
    public static final byte KIND_HEARTBEAT = 0;
    public static final byte KIND_MIST = 1;
    public static final byte KIND_MIST_SHORT = 2;

    public static final Type<ClientboundWarpFXPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "warp_fx"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWarpFXPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BYTE, ClientboundWarpFXPayload::kind, ClientboundWarpFXPayload::new);

    public static ClientboundWarpFXPayload heartbeat() {
        return new ClientboundWarpFXPayload(KIND_HEARTBEAT);
    }

    public static ClientboundWarpFXPayload mist() {
        return new ClientboundWarpFXPayload(KIND_MIST);
    }

    public static ClientboundWarpFXPayload mistShort() {
        return new ClientboundWarpFXPayload(KIND_MIST_SHORT);
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
