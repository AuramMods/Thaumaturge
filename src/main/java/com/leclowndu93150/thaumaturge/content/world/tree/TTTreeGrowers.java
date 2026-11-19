package com.leclowndu93150.thaumaturge.content.world.tree;

import com.leclowndu93150.thaumaturge.TTIds;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class TTTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE_GROWN =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, TTIds.rl("greatwood_tree_grown"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE_GROWN =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, TTIds.rl("silverwood_tree_grown"));

    public static final AbstractTreeGrower GREATWOOD = new AbstractMegaTreeGrower() {
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource random) {
            return GREATWOOD_TREE_GROWN;
        }

        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            random.nextFloat();
            return null;
        }
    };

    public static final AbstractTreeGrower SILVERWOOD = new AbstractTreeGrower() {
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            // The source regular-tree selector samples the secondary chance even without a secondary feature.
            random.nextFloat();
            return SILVERWOOD_TREE_GROWN;
        }
    };

    private static final ImmutableBiMap<String, AbstractTreeGrower> GROWERS =
            ImmutableBiMap.of("thaumaturge:greatwood", GREATWOOD, "thaumaturge:silverwood", SILVERWOOD);
    public static final Codec<AbstractTreeGrower> CODEC =
            ExtraCodecs.stringResolverCodec(GROWERS.inverse()::get, GROWERS::get);

    private TTTreeGrowers() {}

    public static void touch() {}
}
