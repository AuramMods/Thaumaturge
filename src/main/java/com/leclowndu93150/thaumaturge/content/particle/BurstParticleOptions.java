package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record BurstParticleOptions(float scale) implements TTParticleOptions {

    public static final MapCodec<BurstParticleOptions> CODEC = Codec.FLOAT
            .xmap(BurstParticleOptions::new, BurstParticleOptions::scale)
            .fieldOf("scale");

    public static final StreamCodec<ByteBuf, BurstParticleOptions> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(BurstParticleOptions::new, BurstParticleOptions::scale);

    @Override
    public TTParticleType<BurstParticleOptions> getType() {
        return TTParticles.BURST.get();
    }
}
