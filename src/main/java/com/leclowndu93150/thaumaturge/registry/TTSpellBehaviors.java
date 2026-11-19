package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ArchitectDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.AuraDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.BatDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ChainDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.CloudDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.HierophantDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.MineDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ProjectileDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.RayDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SelfDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SprayDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SummonDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.WallDelivery;
import com.leclowndu93150.thaumaturge.content.spell.effect.AspectBurstEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.AspectStrikeEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.BlinkEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.BreakEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.EldritchRendEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.ExchangeEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.HarvestEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.HellbatEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.ImbueEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.PrimalEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.RiftEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.WardEffect;
import com.leclowndu93150.thaumaturge.content.spell.flow.BranchFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.DelayFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.OriginFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.RelayFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.SieveFlow;
import com.leclowndu93150.thaumaturge.content.spell.modifier.EchoModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.FanModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.ScatterModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.StatModifier;
import com.leclowndu93150.thaumaturge.content.spell.script.ScriptedEffect;
import com.leclowndu93150.thaumaturge.content.spell.script.ScriptedNode;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTSpellBehaviors {
    public static final DeferredRegister<SpellBehaviorType<?>> BEHAVIORS =
            DeferredRegister.create(SpellBehaviorType.REGISTRY_KEY, TTIds.MODID);

    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<SpellBehaviorType<?>>> REGISTRY =
            BEHAVIORS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<SpellBehaviorType<?>>().hasTags().disableSync());

    public static final RegistryObject<SpellBehaviorType<OriginFlow>> ORIGIN =
            flow("origin", OriginFlow.CODEC);
    public static final RegistryObject<SpellBehaviorType<BranchFlow>> BRANCH =
            flow("branch", BranchFlow.CODEC);
    public static final RegistryObject<SpellBehaviorType<RelayFlow>> RELAY =
            flow("relay", RelayFlow.CODEC);
    public static final RegistryObject<SpellBehaviorType<DelayFlow>> DELAY =
            flow("delay", DelayFlow.CODEC);
    public static final RegistryObject<SpellBehaviorType<SieveFlow>> SIEVE =
            flow("sieve", SieveFlow.CODEC);

    public static final RegistryObject<SpellBehaviorType<RayDelivery>> RAY =
            delivery("ray", RayDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<ProjectileDelivery>> PROJECTILE =
            delivery("projectile", ProjectileDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<SprayDelivery>> SPRAY =
            delivery("spray", SprayDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<ChainDelivery>> CHAIN =
            delivery("chain", ChainDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<AuraDelivery>> AURA =
            delivery("aura", AuraDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<SelfDelivery>> SELF =
            delivery("self", SelfDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<CloudDelivery>> CLOUD =
            delivery("cloud", CloudDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<MineDelivery>> MINE =
            delivery("mine", MineDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<BatDelivery>> SPELLBAT =
            delivery("spellbat", BatDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<WallDelivery>> WALL =
            delivery("wall", WallDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<SummonDelivery>> SUMMON =
            delivery("summon", SummonDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<ArchitectDelivery>> ARCHITECT =
            delivery("architect", ArchitectDelivery.CODEC);
    public static final RegistryObject<SpellBehaviorType<HierophantDelivery>> HIEROPHANT =
            delivery("hierophant", HierophantDelivery.CODEC);

    public static final RegistryObject<SpellBehaviorType<AspectStrikeEffect>> ASPECT_STRIKE =
            effect("aspect_strike", AspectStrikeEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<AspectBurstEffect>> ASPECT_BURST =
            effect("aspect_burst", AspectBurstEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<ImbueEffect>> IMBUE =
            effect("imbue", ImbueEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<BreakEffect>> BREAK =
            effect("break", BreakEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<ExchangeEffect>> EXCHANGE =
            effect("exchange", ExchangeEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<RiftEffect>> RIFT =
            effect("rift", RiftEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<WardEffect>> WARD =
            effect("ward", WardEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<PrimalEffect>> PRIMAL =
            effect("primal", PrimalEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<HellbatEffect>> HELLBAT =
            effect("hellbat", HellbatEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<BlinkEffect>> BLINK =
            effect("blink", BlinkEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<HarvestEffect>> HARVEST =
            effect("harvest", HarvestEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<EldritchRendEffect>> ELDRITCH_REND =
            effect("eldritch_rend", EldritchRendEffect.CODEC);

    public static final RegistryObject<SpellBehaviorType<StatModifier>> STAT =
            modifier("stat", StatModifier.CODEC);
    public static final RegistryObject<SpellBehaviorType<ScatterModifier>> SCATTER =
            modifier("scatter", ScatterModifier.CODEC);
    public static final RegistryObject<SpellBehaviorType<FanModifier>> FAN =
            modifier("fan", FanModifier.CODEC);
    public static final RegistryObject<SpellBehaviorType<EchoModifier>> ECHO =
            modifier("echo", EchoModifier.CODEC);

    public static final RegistryObject<SpellBehaviorType<ScriptedEffect>> SCRIPTED_EFFECT =
            effect("scripted_effect", ScriptedEffect.CODEC);
    public static final RegistryObject<SpellBehaviorType<ScriptedNode>> SCRIPTED_DELIVERY =
            scripted("scripted_delivery", SpellPartKind.DELIVERY);
    public static final RegistryObject<SpellBehaviorType<ScriptedNode>> SCRIPTED_MODIFIER =
            scripted("scripted_modifier", SpellPartKind.MODIFIER);
    public static final RegistryObject<SpellBehaviorType<ScriptedNode>> SCRIPTED_FLOW =
            scripted("scripted_flow", SpellPartKind.FLOW);

    private TTSpellBehaviors() {}

    private static <B extends SpellBehavior> RegistryObject<SpellBehaviorType<B>> register(
            String path, SpellPartKind kind, MapCodec<B> codec) {
        return BEHAVIORS.register(path, () -> new SpellBehaviorType<>(kind, codec));
    }

    private static <B extends SpellBehavior> RegistryObject<SpellBehaviorType<B>> delivery(
            String path, MapCodec<B> codec) {
        return register(path, SpellPartKind.DELIVERY, codec);
    }

    private static <B extends SpellBehavior> RegistryObject<SpellBehaviorType<B>> effect(
            String path, MapCodec<B> codec) {
        return register(path, SpellPartKind.EFFECT, codec);
    }

    private static <B extends SpellBehavior> RegistryObject<SpellBehaviorType<B>> modifier(
            String path, MapCodec<B> codec) {
        return register(path, SpellPartKind.MODIFIER, codec);
    }

    private static <B extends SpellBehavior> RegistryObject<SpellBehaviorType<B>> flow(
            String path, MapCodec<B> codec) {
        return register(path, SpellPartKind.FLOW, codec);
    }

    private static RegistryObject<SpellBehaviorType<ScriptedNode>> scripted(
            String path, SpellPartKind kind) {
        ResourceLocation id = TTIds.rl(path);
        return register(path, kind, ScriptedNode.codec(() -> REGISTRY.get().getValue(id)));
    }

    public static Registry<SpellBehaviorType<?>> registry() {
        return ForgeRegistration.vanillaRegistry(REGISTRY);
    }

    public static void register(IEventBus modBus) {
        BEHAVIORS.register(modBus);
    }
}
