package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeature;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeatureConfig;
import com.leclowndu93150.thaumaturge.content.manabean.ManaPodFeature;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterConfig;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterFeature;
import com.leclowndu93150.thaumaturge.content.world.crystal.DimensionalCrystalFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.CrimsonPortalFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.HilltopStonesFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.MagicalCaveBushFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.MagicalCavePondFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.ObsidianTotemFeature;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraConfig;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraFeature;
import com.leclowndu93150.thaumaturge.content.world.taint.TaintBiomeConfig;
import com.leclowndu93150.thaumaturge.content.world.taint.TaintBiomeFeature;
import com.leclowndu93150.thaumaturge.content.world.tree.OptionalDynamicTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, TTIds.MODID);

    public static final RegistryObject<OptionalDynamicTreeFeature> COMPAT_TREE =
            FEATURES.register("compat_tree", OptionalDynamicTreeFeature::new);

    public static final RegistryObject<CrystalClusterFeature> CRYSTAL_CLUSTER =
            FEATURES.register("crystal_cluster", () -> new CrystalClusterFeature(CrystalClusterConfig.CODEC));
    public static final RegistryObject<DimensionalCrystalFeature> DIMENSIONAL_CRYSTALS =
            FEATURES.register("dimensional_crystals", DimensionalCrystalFeature::new);

    public static final RegistryObject<MagicForestFloraFeature> MAGIC_FOREST_FLORA =
            FEATURES.register("magic_forest_flora", () -> new MagicForestFloraFeature(MagicForestFloraConfig.CODEC));

    public static final RegistryObject<ManaPodFeature> MANA_PODS =
            FEATURES.register("mana_pods", () -> new ManaPodFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NodeFeatureConfig>> NODE =
            FEATURES.register("node", () -> new NodeFeature(NodeFeatureConfig.CODEC));

    public static final RegistryObject<CrimsonPortalFeature> CRIMSON_PORTAL =
            FEATURES.register("crimson_portal", () -> new CrimsonPortalFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<ObsidianTotemFeature> OBSIDIAN_TOTEM =
            FEATURES.register("obsidian_totem", () -> new ObsidianTotemFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<HilltopStonesFeature> HILLTOP_STONES =
            FEATURES.register("hilltop_stones", () -> new HilltopStonesFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<MagicalCavePondFeature> MAGICAL_CAVE_POND =
            FEATURES.register("magical_cave_pond", () -> new MagicalCavePondFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<MagicalCaveBushFeature> MAGICAL_CAVE_BUSH =
            FEATURES.register("magical_cave_bush", () -> new MagicalCaveBushFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<TaintBiomeFeature> TAINT_BIOME =
            FEATURES.register("taint_biome", () -> new TaintBiomeFeature(TaintBiomeConfig.CODEC));

    private TTFeatures() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
