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

public record WispFlameParticleOptions(int color, float alpha, float scale, float endScale, int delay)
        implements TTParticleOptions {

    public static final MapCodec<WispFlameParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(WispFlameParticleOptions::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(WispFlameParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(WispFlameParticleOptions::scale),
                    Codec.FLOAT.fieldOf("end_scale").forGetter(WispFlameParticleOptions::endScale),
                    Codec.INT.fieldOf("delay").forGetter(WispFlameParticleOptions::delay))
            .apply(inst, WispFlameParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WispFlameParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    WispFlameParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    WispFlameParticleOptions::alpha,
                    ByteBufCodecs.FLOAT,
                    WispFlameParticleOptions::scale,
                    ByteBufCodecs.FLOAT,
                    WispFlameParticleOptions::endScale,
                    ByteBufCodecs.VAR_INT,
                    WispFlameParticleOptions::delay,
                    WispFlameParticleOptions::new);

    @Override
    public TTParticleType<WispFlameParticleOptions> getType() {
        return TTParticles.WISP_FLAME.get();
    }
}
