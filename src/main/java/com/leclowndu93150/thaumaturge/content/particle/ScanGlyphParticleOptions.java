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

public record ScanGlyphParticleOptions(int color, int delay, boolean additive) implements TTParticleOptions {

    public static final MapCodec<ScanGlyphParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(ScanGlyphParticleOptions::color),
                    Codec.INT.fieldOf("delay").forGetter(ScanGlyphParticleOptions::delay),
                    Codec.BOOL.fieldOf("additive").forGetter(ScanGlyphParticleOptions::additive))
            .apply(inst, ScanGlyphParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScanGlyphParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ScanGlyphParticleOptions::color,
                    ByteBufCodecs.VAR_INT,
                    ScanGlyphParticleOptions::delay,
                    ByteBufCodecs.BOOL,
                    ScanGlyphParticleOptions::additive,
                    ScanGlyphParticleOptions::new);

    @Override
    public TTParticleType<ScanGlyphParticleOptions> getType() {
        return TTParticles.SCAN_GLYPH.get();
    }
}
