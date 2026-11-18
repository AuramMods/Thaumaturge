package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record EarthPebbleParticleOptions(float scale) implements TTParticleOptions {

    public static final MapCodec<EarthPebbleParticleOptions> CODEC = Codec.FLOAT
            .xmap(EarthPebbleParticleOptions::new, EarthPebbleParticleOptions::scale)
            .fieldOf("scale");

    public static final StreamCodec<ByteBuf, EarthPebbleParticleOptions> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(EarthPebbleParticleOptions::new, EarthPebbleParticleOptions::scale);

    @Override
    public TTParticleType<EarthPebbleParticleOptions> getType() {
        return TTParticles.EARTH_PEBBLE.get();
    }
}
