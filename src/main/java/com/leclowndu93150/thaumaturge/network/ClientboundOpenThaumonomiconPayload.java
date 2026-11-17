package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundOpenThaumonomiconPayload() implements CustomPacketPayload {
    public static final ClientboundOpenThaumonomiconPayload INSTANCE = new ClientboundOpenThaumonomiconPayload();

    public static final Type<ClientboundOpenThaumonomiconPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "open_thaumonomicon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenThaumonomiconPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
