package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundGolemPressStuffPayload(BlockPos pos, byte[] stuff) implements ModPayload {
    public static final Type<ClientboundGolemPressStuffPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "golem_press_stuff"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundGolemPressStuffPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ClientboundGolemPressStuffPayload::pos,
                    ByteBufCodecs.BYTE_ARRAY,
                    ClientboundGolemPressStuffPayload::stuff,
                    ClientboundGolemPressStuffPayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
