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

public record FluxGooDropletParticleOptions(int color, float alpha, int lifetime) implements TTParticleOptions {
    public static final MapCodec<FluxGooDropletParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(FluxGooDropletParticleOptions::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(FluxGooDropletParticleOptions::alpha),
                    Codec.INT.fieldOf("lifetime").forGetter(FluxGooDropletParticleOptions::lifetime))
            .apply(inst, FluxGooDropletParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluxGooDropletParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    FluxGooDropletParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    FluxGooDropletParticleOptions::alpha,
                    ByteBufCodecs.VAR_INT,
                    FluxGooDropletParticleOptions::lifetime,
                    FluxGooDropletParticleOptions::new);

    @Override
    public TTParticleType<FluxGooDropletParticleOptions> getType() {
        return TTParticles.FLUX_GOO_DROPLET.get();
    }
}
