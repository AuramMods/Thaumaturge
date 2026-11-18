package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record VisSparkleParticleOptions(double tx, double ty, double tz, int color) implements TTParticleOptions {
    public static final int DEFAULT_COLOR = -1;

    public VisSparkleParticleOptions(double tx, double ty, double tz) {
        this(tx, ty, tz, DEFAULT_COLOR);
    }

    public static final MapCodec<VisSparkleParticleOptions> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Codec.DOUBLE.fieldOf("tx").forGetter(VisSparkleParticleOptions::tx),
                            Codec.DOUBLE.fieldOf("ty").forGetter(VisSparkleParticleOptions::ty),
                            Codec.DOUBLE.fieldOf("tz").forGetter(VisSparkleParticleOptions::tz),
                            Codecs.optionalFieldOf(Codec.INT, "color", DEFAULT_COLOR)
                                    .forGetter(VisSparkleParticleOptions::color))
                    .apply(instance, VisSparkleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VisSparkleParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.tx);
                buf.writeDouble(data.ty);
                buf.writeDouble(data.tz);
                buf.writeInt(data.color);
            },
            buf -> new VisSparkleParticleOptions(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt()));

    @Override
    public TTParticleType<VisSparkleParticleOptions> getType() {
        return TTParticles.VIS_SPARKLE.get();
    }
}
