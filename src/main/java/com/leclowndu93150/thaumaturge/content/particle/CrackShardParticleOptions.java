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

public record CrackShardParticleOptions(int color, int variant, float scale, int age) implements TTParticleOptions {

    public static final MapCodec<CrackShardParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(CrackShardParticleOptions::color),
                    Codec.INT.fieldOf("variant").forGetter(CrackShardParticleOptions::variant),
                    Codec.FLOAT.fieldOf("scale").forGetter(CrackShardParticleOptions::scale),
                    Codec.INT.fieldOf("age").forGetter(CrackShardParticleOptions::age))
            .apply(inst, CrackShardParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrackShardParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    CrackShardParticleOptions::color,
                    ByteBufCodecs.VAR_INT,
                    CrackShardParticleOptions::variant,
                    ByteBufCodecs.FLOAT,
                    CrackShardParticleOptions::scale,
                    ByteBufCodecs.VAR_INT,
                    CrackShardParticleOptions::age,
                    CrackShardParticleOptions::new);

    @Override
    public TTParticleType<CrackShardParticleOptions> getType() {
        return TTParticles.CRACK_SHARD.get();
    }
}
