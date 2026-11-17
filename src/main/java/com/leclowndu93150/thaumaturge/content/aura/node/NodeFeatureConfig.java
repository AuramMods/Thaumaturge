package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record NodeFeatureConfig(boolean silverwood, boolean eerie, boolean small, int specialRarity, int baseAura)
        implements FeatureConfiguration {
    public static final Codec<NodeFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "silverwood", false).forGetter(NodeFeatureConfig::silverwood),
                    Codecs.optionalFieldOf(Codec.BOOL, "eerie", false).forGetter(NodeFeatureConfig::eerie),
                    Codecs.optionalFieldOf(Codec.BOOL, "small", false).forGetter(NodeFeatureConfig::small),
                    Codecs.optionalFieldOf(ExtraCodecs.POSITIVE_INT, "special_rarity", NodeGenerator.DEFAULT_SPECIAL_RARITY)
                            .forGetter(NodeFeatureConfig::specialRarity),
                    Codecs.optionalFieldOf(ExtraCodecs.POSITIVE_INT, "base_aura", NodeGenerator.DEFAULT_BASE_AURA)
                            .forGetter(NodeFeatureConfig::baseAura))
            .apply(instance, NodeFeatureConfig::new));
}
