package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import java.util.Optional;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;

public record ClientboundSealPayload(SealPos pos, Optional<SealEntity> seal) implements ModPayload {
    public static final Type<ClientboundSealPayload> TYPE = new Type<>(TTIds.rl("seal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSealPayload> STREAM_CODEC =
            StreamCodec.composite(
                    SealPos.STREAM_CODEC,
                    ClientboundSealPayload::pos,
                    ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistries(SealEntity.CODEC)),
                    ClientboundSealPayload::seal,
                    ClientboundSealPayload::new);

    public static ClientboundSealPayload update(SealEntity seal) {
        return new ClientboundSealPayload(seal.pos(), Optional.of(seal));
    }

    public static ClientboundSealPayload remove(SealPos pos) {
        return new ClientboundSealPayload(pos, Optional.empty());
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
