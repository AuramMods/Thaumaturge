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

public record FlameFanParticleOptions(float scale, float lift, float alpha) implements TTParticleOptions {

    public static final MapCodec<FlameFanParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.FLOAT.fieldOf("scale").forGetter(FlameFanParticleOptions::scale),
                    Codec.FLOAT.fieldOf("lift").forGetter(FlameFanParticleOptions::lift),
                    Codec.FLOAT.fieldOf("alpha").forGetter(FlameFanParticleOptions::alpha))
            .apply(inst, FlameFanParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlameFanParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    FlameFanParticleOptions::scale,
                    ByteBufCodecs.FLOAT,
                    FlameFanParticleOptions::lift,
                    ByteBufCodecs.FLOAT,
                    FlameFanParticleOptions::alpha,
                    FlameFanParticleOptions::new);

    @Override
    public TTParticleType<FlameFanParticleOptions> getType() {
        return TTParticles.FLAME_FAN.get();
    }
}
