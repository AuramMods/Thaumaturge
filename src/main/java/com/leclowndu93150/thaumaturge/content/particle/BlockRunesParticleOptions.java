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

public record BlockRunesParticleOptions(float r, float g, float b, int duration, float gravity, boolean variant)
        implements TTParticleOptions {

    public static final MapCodec<BlockRunesParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.FLOAT.fieldOf("r").forGetter(BlockRunesParticleOptions::r),
                    Codec.FLOAT.fieldOf("g").forGetter(BlockRunesParticleOptions::g),
                    Codec.FLOAT.fieldOf("b").forGetter(BlockRunesParticleOptions::b),
                    Codec.INT.fieldOf("duration").forGetter(BlockRunesParticleOptions::duration),
                    Codec.FLOAT.fieldOf("gravity").forGetter(BlockRunesParticleOptions::gravity),
                    Codec.BOOL.fieldOf("variant").forGetter(BlockRunesParticleOptions::variant))
            .apply(inst, BlockRunesParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockRunesParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    BlockRunesParticleOptions::r,
                    ByteBufCodecs.FLOAT,
                    BlockRunesParticleOptions::g,
                    ByteBufCodecs.FLOAT,
                    BlockRunesParticleOptions::b,
                    ByteBufCodecs.VAR_INT,
                    BlockRunesParticleOptions::duration,
                    ByteBufCodecs.FLOAT,
                    BlockRunesParticleOptions::gravity,
                    ByteBufCodecs.BOOL,
                    BlockRunesParticleOptions::variant,
                    BlockRunesParticleOptions::new);

    @Override
    public TTParticleType<BlockRunesParticleOptions> getType() {
        return TTParticles.BLOCK_RUNES.get();
    }
}
