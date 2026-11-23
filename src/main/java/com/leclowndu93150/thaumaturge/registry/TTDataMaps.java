package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryItem;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.api.items.trim.TrimMaterialBonus;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.warp.ItemWarp;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedProfile;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.Crystallization;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.DistillationRules;
import com.leclowndu93150.thaumaturge.content.golem.GolemAssemblyCosts;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.InscriptionRules;
import com.leclowndu93150.thaumaturge.content.device.sprayer.SprayerRules;
import com.leclowndu93150.thaumaturge.content.device.AuraMachineRules;
import com.leclowndu93150.thaumaturge.content.research.decon.DeconstructionRules;
import com.leclowndu93150.thaumaturge.content.spa.SpaRules;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.PatternCrafterRules;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.RegisterDataMapTypesEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class TTDataMaps {
    public static final DataMapType<Biome, BiomeAuraModifier> BIOME_AURA_MODIFIER = DataMapType.builder(
                    TTIds.rl("aura_modifier"), Registries.BIOME, BiomeAuraModifier.CODEC)
            .synced(BiomeAuraModifier.CODEC, false)
            .build();

    public static final DataMapType<Biome, BiomeAspects> BIOME_ASPECTS = DataMapType.builder(
                    TTIds.rl("biome_aspects"), Registries.BIOME, BiomeAspects.CODEC)
            .build();

    public static final DataMapType<Item, ItemWarp> ITEM_WARP = DataMapType.builder(
                    TTIds.rl("warp"), Registries.ITEM, ItemWarp.CODEC)
            .synced(ItemWarp.CODEC, false)
            .build();

    public static final DataMapType<TrimMaterial, TrimMaterialBonus> TRIM_MATERIAL_BONUS = TrimMaterialBonus.DATA_MAP;

    public static final DataMapType<EntityType<?>, TaintedProfile> TAINTED_PROFILE = TaintedProfile.DATA_MAP;

    private TTDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(Crystallization.DATA_MAP);
        event.register(DistillationRules.SMELTER);
        event.register(DistillationRules.ADVANCED);
        event.register(DeconstructionRules.DATA_MAP);
        event.register(GolemAssemblyCosts.MATERIAL);
        event.register(GolemAssemblyCosts.HEAD);
        event.register(GolemAssemblyCosts.ARM);
        event.register(GolemAssemblyCosts.LEG);
        event.register(GolemAssemblyCosts.ADDON);
        event.register(GolemAssemblyCosts.TRAIT);
        event.register(SpaRules.MIXING);
        event.register(SpaRules.PLACEMENT);
        event.register(PatternCrafterRules.DATA_MAP);
        event.register(BIOME_AURA_MODIFIER);
        event.register(BIOME_ASPECTS);
        event.register(ITEM_WARP);
        event.register(TRIM_MATERIAL_BONUS);
        event.register(TTTrimMaterials.CRYSTAL_MATERIAL);
        event.register(GolemAccessoryItem.DATA_MAP);
        event.register(TAINTED_PROFILE);
        event.register(FocusTier.DATA_MAP);
        event.register(InscriptionRules.DATA_MAP);
        event.register(SprayerRules.DATA_MAP);
        event.register(AuraMachineRules.CONDENSER);
        event.register(AuraMachineRules.VIS_GENERATOR);
        event.register(AuraMachineRules.FLUX_SCRUBBER);
    }
}
