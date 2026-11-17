package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundInfusionSourcePayload(BlockPos matrixPos, BlockPos sourcePos) implements ModPayload {
    public static final Type<ClientboundInfusionSourcePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "fx_infusion_source"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundInfusionSourcePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> {
                        buf.writeBlockPos(data.matrixPos);
                        buf.writeBlockPos(data.sourcePos);
                    },
                    buf -> new ClientboundInfusionSourcePayload(buf.readBlockPos(), buf.readBlockPos()));

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
