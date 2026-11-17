package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;

public record LayoutSettings(
        float coverage,
        float loopRatio,
        int minLoopLength,
        IntProvider branchLength,
        float straightness,
        float keyDistance,
        float costNoise) {
    public static final LayoutSettings DEFAULT =
            new LayoutSettings(0.72F, 0.08F, 8, UniformInt.of(2, 6), 0.6F, 0.6F, 0.5F);

    private static final Codec<Float> UNIT = Codec.floatRange(0.0F, 1.0F);
    private static final int MAX_LOOP_LENGTH = 64;

    public static final Codec<LayoutSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codecs.optionalFieldOf(UNIT, "coverage", DEFAULT.coverage).forGetter(LayoutSettings::coverage),
                    Codecs.optionalFieldOf(UNIT, "loop_ratio", DEFAULT.loopRatio).forGetter(LayoutSettings::loopRatio),
                    Codecs.optionalFieldOf(Codec.intRange(3, MAX_LOOP_LENGTH), "min_loop_length", DEFAULT.minLoopLength)
                            .forGetter(LayoutSettings::minLoopLength),
                    Codecs.optionalFieldOf(IntProvider.POSITIVE_CODEC, "branch_length", DEFAULT.branchLength)
                            .forGetter(LayoutSettings::branchLength),
                    Codecs.optionalFieldOf(UNIT, "straightness", DEFAULT.straightness).forGetter(LayoutSettings::straightness),
                    Codecs.optionalFieldOf(UNIT, "key_distance", DEFAULT.keyDistance).forGetter(LayoutSettings::keyDistance),
                    Codecs.optionalFieldOf(UNIT, "cost_noise", DEFAULT.costNoise).forGetter(LayoutSettings::costNoise))
            .apply(instance, LayoutSettings::new));
}
