package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundAuraSnapshotPayload(int chunkX, int chunkZ, short base, float vis, float flux)
        implements ModPayload {
    public static final Type<ClientboundAuraSnapshotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "aura_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAuraSnapshotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundAuraSnapshotPayload::chunkX,
                    ByteBufCodecs.VAR_INT,
                    ClientboundAuraSnapshotPayload::chunkZ,
                    ByteBufCodecs.SHORT,
                    ClientboundAuraSnapshotPayload::base,
                    ByteBufCodecs.FLOAT,
                    ClientboundAuraSnapshotPayload::vis,
                    ByteBufCodecs.FLOAT,
                    ClientboundAuraSnapshotPayload::flux,
                    ClientboundAuraSnapshotPayload::new);

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
