package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;

public record Centrifugation(boolean enabled, int ticks, Optional<ResourceKey<IAspect>> result) {
    static final int DEFAULT_TICKS = 40;
    private static final Centrifugation DEFAULT = new Centrifugation(true, DEFAULT_TICKS, Optional.empty());
    private static final Codec<Centrifugation> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(Centrifugation::enabled),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1200), "ticks", DEFAULT_TICKS)
                            .forGetter(Centrifugation::ticks),
                    Codecs.optionalFieldOf(ResourceKey.codec(IAspect.REGISTRY_KEY), "result")
                            .forGetter(Centrifugation::result))
            .apply(i, Centrifugation::new));

    public static final DataMapType<IAspect, Centrifugation> DATA_MAP =
            DataMapType.builder(TTIds.rl("centrifugation"), IAspect.REGISTRY_KEY, CODEC).build();

    static Centrifugation of(Holder<IAspect> input) {
        var rule = DataMaps.get(input, DATA_MAP);
        return rule == null ? DEFAULT : rule;
    }
}
