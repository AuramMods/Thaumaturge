package com.leclowndu93150.thaumaturge.content.device.patterncrafter;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public record PatternCrafterRules(boolean enabled, int interval, float vis) {
    private static final int DEFAULT_INTERVAL = 20;
    private static final float DEFAULT_VIS = 1.0F;
    static final PatternCrafterRules DEFAULT = new PatternCrafterRules(true, DEFAULT_INTERVAL, DEFAULT_VIS);
    private static final Codec<PatternCrafterRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(PatternCrafterRules::enabled),
                    Codecs.optionalFieldOf(Codec.intRange(1, Integer.MAX_VALUE), "interval", DEFAULT_INTERVAL)
                            .forGetter(PatternCrafterRules::interval),
                    Codecs.optionalFieldOf(Codec.floatRange(0, Float.MAX_VALUE), "vis", DEFAULT_VIS)
                            .forGetter(PatternCrafterRules::vis))
            .apply(i, PatternCrafterRules::new));

    public static final DataMapType<Block, PatternCrafterRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("pattern_crafter"), Registries.BLOCK, CODEC).build();

    static PatternCrafterRules of(Block block) {
        var rule = DataMaps.get(block.builtInRegistryHolder(), DATA_MAP);
        return rule == null ? DEFAULT : rule;
    }
}
