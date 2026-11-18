package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record CurlyWispParticleOptions(int color, float alpha, float scale, int delay, int seed)
        implements TTParticleOptions {

    public static final MapCodec<CurlyWispParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(CurlyWispParticleOptions::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(CurlyWispParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(CurlyWispParticleOptions::scale),
                    Codec.INT.fieldOf("delay").forGetter(CurlyWispParticleOptions::delay),
                    Codec.INT.fieldOf("seed").forGetter(CurlyWispParticleOptions::seed))
            .apply(inst, CurlyWispParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CurlyWispParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    CurlyWispParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    CurlyWispParticleOptions::alpha,
                    ByteBufCodecs.FLOAT,
                    CurlyWispParticleOptions::scale,
                    ByteBufCodecs.VAR_INT,
                    CurlyWispParticleOptions::delay,
                    ByteBufCodecs.VAR_INT,
                    CurlyWispParticleOptions::seed,
                    CurlyWispParticleOptions::new);

    @Override
    public TTParticleType<CurlyWispParticleOptions> getType() {
        return TTParticles.CURLY_WISP.get();
    }
}
