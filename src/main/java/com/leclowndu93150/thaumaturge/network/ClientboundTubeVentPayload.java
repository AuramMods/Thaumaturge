package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundTubeVentPayload(BlockPos pos, int color) implements ModPayload {
    public static final Type<ClientboundTubeVentPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "tube_vent"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTubeVentPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ClientboundTubeVentPayload::pos,
                    ByteBufCodecs.INT,
                    ClientboundTubeVentPayload::color,
                    ClientboundTubeVentPayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
