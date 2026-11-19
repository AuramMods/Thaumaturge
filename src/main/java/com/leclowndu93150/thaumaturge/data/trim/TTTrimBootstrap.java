package com.leclowndu93150.thaumaturge.data.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimPatterns;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectDefinition;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;

public final class TTTrimBootstrap {
    public static final String CRYSTAL_DESCRIPTION_KEY =
            Util.makeDescriptionId("trim_material", TTIds.rl("essentia_crystal"));
    private static final String MATERIAL_ASSET_PREFIX = "thaumaturge_";
    private static final int BRASS_COLOR = 0xE8C24C;
    private static final int THAUMIUM_COLOR = 0x8A72C8;
    private static final int VOID_METAL_COLOR = 0x6B4E8C;
    private static final float BRASS_MODEL_INDEX = 0.031F;
    private static final float THAUMIUM_MODEL_INDEX = 0.032F;
    private static final float VOID_METAL_MODEL_INDEX = 0.033F;
    private static final float CRYSTAL_MODEL_INDEX = 0.034F;

    public static final List<TrimItemModel> MATERIAL_MODELS = List.of(
            new TrimItemModel(assetName(TTTrimMaterials.BRASS), BRASS_MODEL_INDEX),
            new TrimItemModel(assetName(TTTrimMaterials.THAUMIUM), THAUMIUM_MODEL_INDEX),
            new TrimItemModel(assetName(TTTrimMaterials.VOID_METAL), VOID_METAL_MODEL_INDEX),
            new TrimItemModel(TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET, CRYSTAL_MODEL_INDEX));

    private TTTrimBootstrap() {}

    public static void materials(BootstapContext<TrimMaterial> context) {
        material(context, TTTrimMaterials.BRASS, TTItems.INGOT_BRASS.get(), BRASS_COLOR, BRASS_MODEL_INDEX);
        material(context, TTTrimMaterials.THAUMIUM, TTItems.INGOT_THAUMIUM.get(), THAUMIUM_COLOR, THAUMIUM_MODEL_INDEX);
        material(
                context,
                TTTrimMaterials.VOID_METAL,
                TTItems.INGOT_VOID.get(),
                VOID_METAL_COLOR,
                VOID_METAL_MODEL_INDEX);
        for (AspectDefinition aspect : AspectBootstrap.DEFINITIONS) {
            Component description =
                    Component.translatable(CRYSTAL_DESCRIPTION_KEY).withStyle(Style.EMPTY.withColor(aspect.color()));
            context.register(
                    TTTrimMaterials.essentiaCrystal(aspect.key()),
                    TrimMaterial.create(
                            TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET,
                            TTItems.ESSENTIA_CRYSTAL.get(),
                            CRYSTAL_MODEL_INDEX,
                            description,
                            Map.of()));
        }
    }

    public static void patterns(BootstapContext<TrimPattern> context) {
        pattern(context, TTTrimPatterns.ARCANE, TTItems.ARCANE_ARMOR_TRIM_SMITHING_TEMPLATE.get());
        pattern(context, TTTrimPatterns.OCCULT, TTItems.OCCULT_ARMOR_TRIM_SMITHING_TEMPLATE.get());
    }

    private static void pattern(BootstapContext<TrimPattern> context, ResourceKey<TrimPattern> key, Item template) {
        Component description = Component.translatable(Util.makeDescriptionId("trim_pattern", key.location()));
        context.register(key, new TrimPattern(key.location(), BuiltInRegistries.ITEM.wrapAsHolder(template), description));
    }

    private static void material(
            BootstapContext<TrimMaterial> context,
            ResourceKey<TrimMaterial> key,
            Item ingredient,
            int color,
            float itemModelIndex) {
        Component description = Component.translatable(Util.makeDescriptionId("trim_material", key.location()))
                .withStyle(Style.EMPTY.withColor(color));
        context.register(key, TrimMaterial.create(assetName(key), ingredient, itemModelIndex, description, Map.of()));
    }

    private static String assetName(ResourceKey<TrimMaterial> key) {
        return MATERIAL_ASSET_PREFIX + key.location().getPath();
    }
}
