package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundUpdateJEIAspectListPayload() implements ModPayload {
    public static final ClientboundUpdateJEIAspectListPayload INSTANCE = new ClientboundUpdateJEIAspectListPayload();
    public static final Type<ClientboundUpdateJEIAspectListPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "update_jei_aspect_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateJEIAspectListPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
