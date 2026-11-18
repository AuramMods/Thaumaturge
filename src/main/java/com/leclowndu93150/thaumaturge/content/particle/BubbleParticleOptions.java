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

public record BubbleParticleOptions(int color, float alpha, float scale, int age, float buoyancy, boolean droplet)
        implements TTParticleOptions {

    public static final MapCodec<BubbleParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(BubbleParticleOptions::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(BubbleParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(BubbleParticleOptions::scale),
                    Codec.INT.fieldOf("age").forGetter(BubbleParticleOptions::age),
                    Codec.FLOAT.fieldOf("buoyancy").forGetter(BubbleParticleOptions::buoyancy),
                    Codec.BOOL.fieldOf("droplet").forGetter(BubbleParticleOptions::droplet))
            .apply(inst, BubbleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BubbleParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    BubbleParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    BubbleParticleOptions::alpha,
                    ByteBufCodecs.FLOAT,
                    BubbleParticleOptions::scale,
                    ByteBufCodecs.VAR_INT,
                    BubbleParticleOptions::age,
                    ByteBufCodecs.FLOAT,
                    BubbleParticleOptions::buoyancy,
                    ByteBufCodecs.BOOL,
                    BubbleParticleOptions::droplet,
                    BubbleParticleOptions::new);

    @Override
    public TTParticleType<BubbleParticleOptions> getType() {
        return TTParticles.BUBBLE.get();
    }
}
