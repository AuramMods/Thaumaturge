package com.leclowndu93150.thaumaturge.compat.apothicenchanting;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnchantingStats(float maxEterna, float eterna, float quanta, float arcana, int clues) {
    public static final Codec<EnchantingStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("maxEterna").forGetter(EnchantingStats::maxEterna),
                    Codecs.optionalFieldOf(Codec.FLOAT, "eterna", 0.0F).forGetter(EnchantingStats::eterna),
                    Codecs.optionalFieldOf(Codec.FLOAT, "quanta", 0.0F).forGetter(EnchantingStats::quanta),
                    Codecs.optionalFieldOf(Codec.FLOAT, "arcana", 0.0F).forGetter(EnchantingStats::arcana),
                    Codecs.optionalFieldOf(Codec.INT, "clues", 0).forGetter(EnchantingStats::clues))
            .apply(instance, EnchantingStats::new));
}
