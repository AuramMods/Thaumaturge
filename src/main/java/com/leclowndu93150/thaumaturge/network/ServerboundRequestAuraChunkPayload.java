package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundRequestAuraChunkPayload(int chunkX, int chunkZ) implements CustomPacketPayload {
    public static final Type<ServerboundRequestAuraChunkPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "request_aura_chunk"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestAuraChunkPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ServerboundRequestAuraChunkPayload::chunkX,
                    ByteBufCodecs.VAR_INT,
                    ServerboundRequestAuraChunkPayload::chunkZ,
                    ServerboundRequestAuraChunkPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
