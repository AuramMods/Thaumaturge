package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.ArmoredChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.EffectChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.FieryChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.InfestedChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.ModifierChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.SpinedChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.UndyingChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.VampiricChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.WardedChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.WarpChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.trait.LeapingTrait;
import com.leclowndu93150.thaumaturge.content.entity.trait.MarkerTrait;
import com.leclowndu93150.thaumaturge.content.entity.trait.StrikeEffectTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintBroodTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintGrazingTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.ability.TaintLayingTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.ability.TaintacleLureTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.ability.WarpingStrikeTrait;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTMobTraits {
    private static final double BOLD_SPEED = 0.3;
    private static final double MIGHTY_DAMAGE = 2.0;
    private static final int GRIM_WITHER_TICKS = 200;
    private static final int SICKLY_HUNGER_TICKS = 500;
    private static final int VENOMOUS_POISON_TICKS = 100;
    private static final int LIFTING_LEVITATION_TICKS = 60;
    private static final int LIFTING_LEVITATION_AMPLIFIER = 1;

    public static final DeferredRegister<MobTrait> TRAITS = DeferredRegister.create(MobTrait.REGISTRY_KEY, TTIds.MODID);

    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<MobTrait>> REGISTRY =
            TRAITS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<MobTrait>().hasTags());

    public static final RegistryObject<ModifierChampionTrait> BOLD = TRAITS.register(
            "bold",
            () -> new ModifierChampionTrait(
                    Attributes.MOVEMENT_SPEED, BOLD_SPEED, AttributeModifier.Operation.MULTIPLY_BASE));
    public static final RegistryObject<SpinedChampionTrait> SPINED =
            TRAITS.register("spine", SpinedChampionTrait::new);
    public static final RegistryObject<ArmoredChampionTrait> ARMORED =
            TRAITS.register("armor", ArmoredChampionTrait::new);
    public static final RegistryObject<ModifierChampionTrait> MIGHTY = TRAITS.register(
            "mighty",
            () -> new ModifierChampionTrait(
                    Attributes.ATTACK_DAMAGE, MIGHTY_DAMAGE, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<EffectChampionTrait> GRIM =
            TRAITS.register("grim", () -> new EffectChampionTrait(EffectCodecs.holder(MobEffects.WITHER), GRIM_WITHER_TICKS));
    public static final RegistryObject<WardedChampionTrait> WARDED =
            TRAITS.register("warded", WardedChampionTrait::new);
    public static final RegistryObject<WarpChampionTrait> WARP =
            TRAITS.register("warp", WarpChampionTrait::new);
    public static final RegistryObject<UndyingChampionTrait> UNDYING =
            TRAITS.register("undying", UndyingChampionTrait::new);
    public static final RegistryObject<FieryChampionTrait> FIERY =
            TRAITS.register("fiery", FieryChampionTrait::new);
    public static final RegistryObject<EffectChampionTrait> SICKLY =
            TRAITS.register("sickly", () -> new EffectChampionTrait(EffectCodecs.holder(MobEffects.HUNGER), SICKLY_HUNGER_TICKS));
    public static final RegistryObject<EffectChampionTrait> VENOMOUS =
            TRAITS.register("venomous", () -> new EffectChampionTrait(EffectCodecs.holder(MobEffects.POISON), VENOMOUS_POISON_TICKS));
    public static final RegistryObject<VampiricChampionTrait> VAMPIRIC =
            TRAITS.register("vampiric", VampiricChampionTrait::new);
    public static final RegistryObject<InfestedChampionTrait> INFESTED =
            TRAITS.register("infested", InfestedChampionTrait::new);

    public static final RegistryObject<TaintedTrait> TAINTED = TRAITS.register("tainted", TaintedTrait::new);
    public static final RegistryObject<TaintGrazingTrait> TAINT_GRAZING =
            TRAITS.register("taint_grazing", TaintGrazingTrait::new);
    public static final RegistryObject<MarkerTrait> TAINT_BLAST =
            TRAITS.register("taint_blast", MarkerTrait::new);
    public static final RegistryObject<TaintBroodTrait> TAINT_BROOD =
            TRAITS.register("taint_brood", TaintBroodTrait::new);
    public static final RegistryObject<LeapingTrait> LEAPING = TRAITS.register("leaping", LeapingTrait::new);
    public static final RegistryObject<MarkerTrait> TAINT_SHEARING =
            TRAITS.register("taint_shearing", MarkerTrait::new);
    public static final RegistryObject<MarkerTrait> FLUX_MILK =
            TRAITS.register("flux_milk", MarkerTrait::new);
    public static final RegistryObject<TaintLayingTrait> TAINT_LAYING =
            TRAITS.register("taint_laying", TaintLayingTrait::new);
    public static final RegistryObject<MarkerTrait> SWARM_BURST =
            TRAITS.register("swarm_burst", MarkerTrait::new);
    public static final RegistryObject<WarpingStrikeTrait> WARPING_STRIKE =
            TRAITS.register("warping_strike", WarpingStrikeTrait::new);
    public static final RegistryObject<MarkerTrait> TAINT_ARROWS =
            TRAITS.register("taint_arrows", MarkerTrait::new);
    public static final RegistryObject<MarkerTrait> TAINT_BREWING =
            TRAITS.register("taint_brewing", MarkerTrait::new);
    public static final RegistryObject<StrikeEffectTrait> LIFTING_STRIKE = TRAITS.register(
            "lifting_strike",
            () -> new StrikeEffectTrait(EffectCodecs.holder(MobEffects.LEVITATION), LIFTING_LEVITATION_TICKS, LIFTING_LEVITATION_AMPLIFIER));
    public static final RegistryObject<TaintacleLureTrait> TAINTACLE_LURE =
            TRAITS.register("taintacle_lure", TaintacleLureTrait::new);

    private TTMobTraits() {}

    public static Registry<MobTrait> registry() {
        return ForgeRegistration.vanillaRegistry(REGISTRY);
    }

    public static ResourceKey<MobTrait> key(RegistryObject<? extends MobTrait> trait) {
        return ForgeRegistration.entryKey(MobTrait.REGISTRY_KEY, trait);
    }

    public static Holder.Reference<MobTrait> holder(RegistryObject<? extends MobTrait> trait) {
        return registry().getHolderOrThrow(key(trait));
    }

    public static void register(IEventBus modBus) {
        TRAITS.register(modBus);
    }
}
