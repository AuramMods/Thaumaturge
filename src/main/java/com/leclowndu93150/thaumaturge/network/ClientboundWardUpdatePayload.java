package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundWardUpdatePayload(BlockPos pos, Optional<UUID> owner) implements ModPayload {
    public static final Type<ClientboundWardUpdatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "ward_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWardUpdatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ClientboundWardUpdatePayload::pos,
                    ByteBufCodecs.optional(VanillaStreamCodecs.UUID),
                    ClientboundWardUpdatePayload::owner,
                    ClientboundWardUpdatePayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
