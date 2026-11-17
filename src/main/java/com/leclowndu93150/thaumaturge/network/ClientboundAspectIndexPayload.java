package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndex;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundAspectIndexPayload(AspectIndex index) implements ModPayload {
    public static final ModPayload.Type<ClientboundAspectIndexPayload> TYPE =
            new ModPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "aspect_index"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAspectIndexPayload> STREAM_CODEC =
            AspectIndex.STREAM_CODEC.map(ClientboundAspectIndexPayload::new, ClientboundAspectIndexPayload::index);

    @Override
    public ModPayload.Type<? extends ModPayload> type() {
        return TYPE;
    }
}
