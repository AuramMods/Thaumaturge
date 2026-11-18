package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record StabilizerRuneParticleOptions(int life) implements TTParticleOptions {

    public static final MapCodec<StabilizerRuneParticleOptions> CODEC = Codec.INT
            .xmap(StabilizerRuneParticleOptions::new, StabilizerRuneParticleOptions::life)
            .fieldOf("life");

    public static final StreamCodec<ByteBuf, StabilizerRuneParticleOptions> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(StabilizerRuneParticleOptions::new, StabilizerRuneParticleOptions::life);

    @Override
    public TTParticleType<StabilizerRuneParticleOptions> getType() {
        return TTParticles.STABILIZER_RUNE.get();
    }
}
