package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record ShieldSparkParticleOptions(int color, float alpha, float scale, int age, int delay, boolean additive)
        implements TTParticleOptions {

    public static final MapCodec<ShieldSparkParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(ShieldSparkParticleOptions::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(ShieldSparkParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(ShieldSparkParticleOptions::scale),
                    Codec.INT.fieldOf("age").forGetter(ShieldSparkParticleOptions::age),
                    Codecs.optionalFieldOf(Codec.INT, "delay", 0).forGetter(ShieldSparkParticleOptions::delay),
                    Codec.BOOL.fieldOf("additive").forGetter(ShieldSparkParticleOptions::additive))
            .apply(inst, ShieldSparkParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldSparkParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ShieldSparkParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    ShieldSparkParticleOptions::alpha,
                    ByteBufCodecs.FLOAT,
                    ShieldSparkParticleOptions::scale,
                    ByteBufCodecs.VAR_INT,
                    ShieldSparkParticleOptions::age,
                    ByteBufCodecs.VAR_INT,
                    ShieldSparkParticleOptions::delay,
                    ByteBufCodecs.BOOL,
                    ShieldSparkParticleOptions::additive,
                    ShieldSparkParticleOptions::new);

    @Override
    public TTParticleType<ShieldSparkParticleOptions> getType() {
        return TTParticles.SHIELD_SPARK.get();
    }
}
