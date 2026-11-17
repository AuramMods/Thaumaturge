package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundTaintEnvironmentPayload(float pressure) implements ModPayload {
    public static final Type<ClientboundTaintEnvironmentPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "taint_environment"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTaintEnvironmentPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    ClientboundTaintEnvironmentPayload::pressure,
                    ClientboundTaintEnvironmentPayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
