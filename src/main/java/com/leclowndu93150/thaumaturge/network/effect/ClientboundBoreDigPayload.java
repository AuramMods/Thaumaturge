package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;

public record ClientboundBoreDigPayload(BlockPos target, int boreEntityId, BlockPos borePos, int delay)
        implements ModPayload {
    public static final Type<ClientboundBoreDigPayload> TYPE = new Type<>(TTIds.rl("bore_dig"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBoreDigPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ClientboundBoreDigPayload::target,
                    ByteBufCodecs.VAR_INT,
                    ClientboundBoreDigPayload::boreEntityId,
                    VanillaStreamCodecs.BLOCK_POS,
                    ClientboundBoreDigPayload::borePos,
                    ByteBufCodecs.VAR_INT,
                    ClientboundBoreDigPayload::delay,
                    ClientboundBoreDigPayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
