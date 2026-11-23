package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public record NodeJarringRules(boolean enabled, int visPerPrimal, float degradeChance) {
    private static final int DEFAULT_VIS = 70;
    static final float DEFAULT_DEGRADE_CHANCE = 0.75F;
    private static final NodeJarringRules DEFAULT = new NodeJarringRules(true, DEFAULT_VIS, DEFAULT_DEGRADE_CHANCE);
    private static final Codec<NodeJarringRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(NodeJarringRules::enabled),
                    Codecs.optionalFieldOf(Codec.intRange(0, 1000), "vis_per_primal", DEFAULT_VIS)
                            .forGetter(NodeJarringRules::visPerPrimal),
                    Codecs.optionalFieldOf(Codec.floatRange(0.0F, 1.0F), "degrade_chance", DEFAULT_DEGRADE_CHANCE)
                            .forGetter(NodeJarringRules::degradeChance))
            .apply(i, NodeJarringRules::new));

    public static final DataMapType<Block, NodeJarringRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("node_jarring"), Registries.BLOCK, CODEC).build();

    static NodeJarringRules of(Block block) {
        NodeJarringRules rules = DataMaps.get(block.builtInRegistryHolder(), DATA_MAP);
        return rules == null ? DEFAULT : rules;
    }
}
