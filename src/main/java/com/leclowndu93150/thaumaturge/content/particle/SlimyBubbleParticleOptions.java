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

public record SlimyBubbleParticleOptions(int color, float alpha, float scale, int age) implements TTParticleOptions {

    public static final MapCodec<SlimyBubbleParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(SlimyBubbleParticleOptions::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(SlimyBubbleParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(SlimyBubbleParticleOptions::scale),
                    Codec.INT.fieldOf("age").forGetter(SlimyBubbleParticleOptions::age))
            .apply(inst, SlimyBubbleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlimyBubbleParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    SlimyBubbleParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    SlimyBubbleParticleOptions::alpha,
                    ByteBufCodecs.FLOAT,
                    SlimyBubbleParticleOptions::scale,
                    ByteBufCodecs.VAR_INT,
                    SlimyBubbleParticleOptions::age,
                    SlimyBubbleParticleOptions::new);

    @Override
    public TTParticleType<SlimyBubbleParticleOptions> getType() {
        return TTParticles.SLIMY_BUBBLE.get();
    }
}
