package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedProfile;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.RegistryObject;
import com.leclowndu93150.thaumaturge.datamap.DataMapProvider;

public final class TaintedProfileProvider extends DataMapProvider {
    private static final double FOLLOW_RANGE = 24.0;
    private static final double LIVESTOCK_ARMOR = 2.0;

    public TaintedProfileProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public String getName() {
        return "Tainted Profile Data Maps";
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<TaintedProfile, EntityType<?>> b = builder(TTDataMaps.TAINTED_PROFILE);
        HolderGetter<MobTrait> traits = provider.lookupOrThrow(MobTrait.REGISTRY_KEY);
        add(
                b,
                EntityType.COW,
                new TaintedProfile(
                        stats(40.0, 6.0, 0.27, 0.0),
                        true,
                        true,
                        List.of(trait(traits, TTMobTraits.FLUX_MILK)),
                        true,
                        Optional.of(TTLootTables.TAINTED_COW)));
        add(
                b,
                EntityType.PIG,
                new TaintedProfile(
                        stats(20.0, 4.0, 0.275, LIVESTOCK_ARMOR),
                        true,
                        true,
                        List.of(trait(traits, TTMobTraits.SWARM_BURST)),
                        true,
                        Optional.of(TTLootTables.TAINTED_PIG)));
        add(
                b,
                EntityType.CHICKEN,
                new TaintedProfile(
                        stats(8.0, 3.0, 0.4, LIVESTOCK_ARMOR),
                        true,
                        true,
                        List.of(trait(traits, TTMobTraits.LEAPING), trait(traits, TTMobTraits.TAINT_LAYING)),
                        true,
                        Optional.of(TTLootTables.TAINTED_CHICKEN)));
        add(
                b,
                EntityType.SHEEP,
                new TaintedProfile(
                        stats(20.0, 3.0, 0.25, LIVESTOCK_ARMOR),
                        true,
                        false,
                        List.of(trait(traits, TTMobTraits.TAINT_GRAZING), trait(traits, TTMobTraits.TAINT_SHEARING)),
                        true,
                        Optional.of(TTLootTables.TAINTED_SHEEP)));
        add(
                b,
                EntityType.VILLAGER,
                new TaintedProfile(
                        stats(30.0, 4.0, 0.3, 0.0),
                        false,
                        false,
                        List.of(),
                        true,
                        Optional.of(TTLootTables.TAINTED_VILLAGER)));
        Map<Holder<Attribute>, Double> creeper = new LinkedHashMap<>();
        creeper.put(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.MAX_HEALTH), 24.0);
        creeper.put(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.MOVEMENT_SPEED), 0.28);
        creeper.put(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.FOLLOW_RANGE), FOLLOW_RANGE);
        add(
                b,
                EntityType.CREEPER,
                new TaintedProfile(
                        creeper,
                        false,
                        false,
                        List.of(trait(traits, TTMobTraits.TAINT_BLAST)),
                        false,
                        Optional.of(TTLootTables.TAINTED_CREEPER)));
        add(b, EntityType.ZOMBIE, hostile(trait(traits, TTMobTraits.WARPING_STRIKE), true));
        add(b, EntityType.SKELETON, hostile(trait(traits, TTMobTraits.TAINT_ARROWS), true));
        add(b, EntityType.PILLAGER, hostile(trait(traits, TTMobTraits.TAINT_ARROWS), false));
        add(b, EntityType.WITCH, hostile(trait(traits, TTMobTraits.TAINT_BREWING), true));
        add(b, EntityType.PHANTOM, hostile(trait(traits, TTMobTraits.LIFTING_STRIKE), true));
        add(b, EntityType.ENDERMAN, hostile(trait(traits, TTMobTraits.TAINTACLE_LURE), true));
    }

    private static Holder<MobTrait> trait(HolderGetter<MobTrait> traits, RegistryObject<? extends MobTrait> entry) {
        return traits.getOrThrow(TTMobTraits.key(entry));
    }

    private static TaintedProfile hostile(Holder<MobTrait> trait, boolean naturalSpawns) {
        return new TaintedProfile(Map.of(), false, false, List.of(trait), naturalSpawns, Optional.empty());
    }

    private static Map<Holder<Attribute>, Double> stats(double health, double attack, double speed, double armor) {
        Map<Holder<Attribute>, Double> stats = new LinkedHashMap<>();
        stats.put(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.MAX_HEALTH), health);
        stats.put(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.ATTACK_DAMAGE), attack);
        stats.put(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.MOVEMENT_SPEED), speed);
        if (armor > 0.0) {
            stats.put(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.ARMOR), armor);
        }
        stats.put(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.FOLLOW_RANGE), FOLLOW_RANGE);
        return stats;
    }

    private static void add(
            Builder<TaintedProfile, EntityType<?>> builder, EntityType<?> type, TaintedProfile profile) {
        builder.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type), profile, false);
    }
}
