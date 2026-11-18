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

public record FluxSwirlParticleOptions(int color, float scale, float endScale) implements TTParticleOptions {

    public static final MapCodec<FluxSwirlParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(FluxSwirlParticleOptions::color),
                    Codec.FLOAT.fieldOf("scale").forGetter(FluxSwirlParticleOptions::scale),
                    Codec.FLOAT.fieldOf("end_scale").forGetter(FluxSwirlParticleOptions::endScale))
            .apply(inst, FluxSwirlParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluxSwirlParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    FluxSwirlParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    FluxSwirlParticleOptions::scale,
                    ByteBufCodecs.FLOAT,
                    FluxSwirlParticleOptions::endScale,
                    FluxSwirlParticleOptions::new);

    @Override
    public TTParticleType<FluxSwirlParticleOptions> getType() {
        return TTParticles.FLUX_SWIRL.get();
    }
}
