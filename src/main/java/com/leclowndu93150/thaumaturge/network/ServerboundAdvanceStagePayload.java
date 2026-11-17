package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundAdvanceStagePayload(ResourceLocation research) implements ModPayload {
    public static final Type<ServerboundAdvanceStagePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "advance_stage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAdvanceStagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.RESOURCE_LOCATION,
                    ServerboundAdvanceStagePayload::research,
                    ServerboundAdvanceStagePayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
