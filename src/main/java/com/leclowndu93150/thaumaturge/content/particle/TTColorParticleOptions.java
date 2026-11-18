package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.compat.Colors.ARGB32;
import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;

public record TTColorParticleOptions(TTParticleType<TTColorParticleOptions> type, int color)
        implements TTParticleOptions {
    private static final Codec<Integer> COLOR_CODEC = Codecs.withAlternative(Codec.INT,
            Codecs.validate(Codec.FLOAT.listOf(), channels -> Util.fixedSize(channels, 4)),
            rgba -> ARGB32.colorFromFloat(rgba.get(3), rgba.get(0), rgba.get(1), rgba.get(2)));

    public static MapCodec<TTColorParticleOptions> codec(TTParticleType<TTColorParticleOptions> type) {
        return COLOR_CODEC.xmap(color -> new TTColorParticleOptions(type, color), TTColorParticleOptions::color)
                .fieldOf("color");
    }

    public static StreamCodec<ByteBuf, TTColorParticleOptions> streamCodec(TTParticleType<TTColorParticleOptions> type) {
        return ByteBufCodecs.INT.map(color -> new TTColorParticleOptions(type, color), TTColorParticleOptions::color);
    }

    public static TTColorParticleOptions create(TTParticleType<TTColorParticleOptions> type, float r, float g, float b) {
        return new TTColorParticleOptions(type, ARGB32.colorFromFloat(1.0F, r, g, b));
    }

    @Override
    public TTParticleType<TTColorParticleOptions> getType() {
        return type;
    }

    public float getRed() {
        return ARGB32.red(color) / 255.0F;
    }

    public float getGreen() {
        return ARGB32.green(color) / 255.0F;
    }

    public float getBlue() {
        return ARGB32.blue(color) / 255.0F;
    }

    public float getAlpha() {
        return ARGB32.alpha(color) / 255.0F;
    }
}
