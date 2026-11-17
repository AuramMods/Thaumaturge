package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record InfusionModifier(int cycleTime, float cost, float stability) {
    public static final Codec<InfusionModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codecs.optionalFieldOf(Codec.INT, "cycle_time", 0).forGetter(InfusionModifier::cycleTime),
                    Codecs.optionalFieldOf(Codec.FLOAT, "cost", 0.0F).forGetter(InfusionModifier::cost),
                    Codecs.optionalFieldOf(Codec.FLOAT, "stability", 0.0F).forGetter(InfusionModifier::stability))
            .apply(instance, InfusionModifier::new));
}
