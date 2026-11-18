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

public record NitorCoreParticleOptions(float r, float g, float b) implements TTParticleOptions {
    public static final MapCodec<NitorCoreParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.FLOAT.fieldOf("r").forGetter(NitorCoreParticleOptions::r),
                    Codec.FLOAT.fieldOf("g").forGetter(NitorCoreParticleOptions::g),
                    Codec.FLOAT.fieldOf("b").forGetter(NitorCoreParticleOptions::b))
            .apply(inst, NitorCoreParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NitorCoreParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    NitorCoreParticleOptions::r,
                    ByteBufCodecs.FLOAT,
                    NitorCoreParticleOptions::g,
                    ByteBufCodecs.FLOAT,
                    NitorCoreParticleOptions::b,
                    NitorCoreParticleOptions::new);

    @Override
    public TTParticleType<NitorCoreParticleOptions> getType() {
        return TTParticles.NITOR_CORE.get();
    }
}
