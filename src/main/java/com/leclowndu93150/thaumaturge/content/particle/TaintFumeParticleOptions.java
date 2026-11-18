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

public record TaintFumeParticleOptions(int color, float scale) implements TTParticleOptions {

    public static final int RANDOM_COLOR = -1;

    public static final MapCodec<TaintFumeParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(TaintFumeParticleOptions::color),
                    Codec.FLOAT.fieldOf("scale").forGetter(TaintFumeParticleOptions::scale))
            .apply(inst, TaintFumeParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TaintFumeParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    TaintFumeParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    TaintFumeParticleOptions::scale,
                    TaintFumeParticleOptions::new);

    @Override
    public TTParticleType<TaintFumeParticleOptions> getType() {
        return TTParticles.TAINT_FUME.get();
    }
}
