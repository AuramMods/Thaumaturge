package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public record UrnWaterRules(boolean enabled, float maxVis, int waterPerVis, int interval, int pushAmount,
                            int cauldronCost) {
    static final UrnWaterRules DEFAULT = new UrnWaterRules(true, 0.1F, 1000, 5, 25, 333);
    private static final Codec<UrnWaterRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(UrnWaterRules::enabled),
                    Codecs.optionalFieldOf(Codec.floatRange(0.0F, 100.0F), "max_vis", DEFAULT.maxVis())
                            .forGetter(UrnWaterRules::maxVis),
                    Codecs.optionalFieldOf(Codec.intRange(1, 10000), "water_per_vis", DEFAULT.waterPerVis())
                            .forGetter(UrnWaterRules::waterPerVis),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1200), "interval", DEFAULT.interval())
                            .forGetter(UrnWaterRules::interval),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1000), "push_amount", DEFAULT.pushAmount())
                            .forGetter(UrnWaterRules::pushAmount),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1000), "cauldron_cost", DEFAULT.cauldronCost())
                            .forGetter(UrnWaterRules::cauldronCost))
            .apply(i, UrnWaterRules::new));

    public static final DataMapType<Block, UrnWaterRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("everfull_urn"), Registries.BLOCK, CODEC).build();
}
