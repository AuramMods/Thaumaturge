package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.registry.TTFeatures;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public final class DimensionalCrystalFeature extends Feature<DimensionalCrystalFeature.Config> {
    public record Config(Map<ResourceKey<Level>, CrystalClusterConfig> dimensions) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = Codec.unboundedMap(
                        ResourceKey.codec(Registries.DIMENSION), CrystalClusterConfig.CODEC)
                .fieldOf("dimensions")
                .xmap(Config::new, Config::dimensions)
                .codec();
    }

    public DimensionalCrystalFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        var level = context.level();
        var crystals = context.config().dimensions().get(level.getLevel().dimension());
        if (crystals == null) return false;

        return TTFeatures.CRYSTAL_CLUSTER.get().place(new FeaturePlaceContext<>(
                context.topFeature(), level, context.chunkGenerator(), context.random(), context.origin(), crystals));
    }
}
