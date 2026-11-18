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

public record SlashParticleOptions(int duration, float yaw, float pitch, float roll) implements TTParticleOptions {

    public static final MapCodec<SlashParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("duration").forGetter(SlashParticleOptions::duration),
                    Codec.FLOAT.fieldOf("yaw").forGetter(SlashParticleOptions::yaw),
                    Codec.FLOAT.fieldOf("pitch").forGetter(SlashParticleOptions::pitch),
                    Codec.FLOAT.fieldOf("roll").forGetter(SlashParticleOptions::roll))
            .apply(inst, SlashParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlashParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SlashParticleOptions::duration,
            ByteBufCodecs.FLOAT,
            SlashParticleOptions::yaw,
            ByteBufCodecs.FLOAT,
            SlashParticleOptions::pitch,
            ByteBufCodecs.FLOAT,
            SlashParticleOptions::roll,
            SlashParticleOptions::new);

    @Override
    public TTParticleType<SlashParticleOptions> getType() {
        return TTParticles.SLASH.get();
    }
}
