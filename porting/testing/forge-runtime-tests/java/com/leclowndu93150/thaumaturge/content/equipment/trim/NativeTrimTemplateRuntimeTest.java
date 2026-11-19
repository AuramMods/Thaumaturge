package com.leclowndu93150.thaumaturge.content.equipment.trim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimPatterns;
import com.leclowndu93150.thaumaturge.data.trim.TrimItemModel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NativeTrimTemplateRuntimeTest {
    private static SmithingTemplateItem arcane;
    private static SmithingTemplateItem occult;

    @BeforeAll
    static void registerThroughForge() {
        var items = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var registry = (MappedRegistry<?>) BuiltInRegistries.ITEM;
        items.unfreeze();
        registry.unfreeze();
        try {
            arcane = TrimTemplateRarity.create(TTTrimPatterns.ARCANE, Rarity.UNCOMMON);
            occult = TrimTemplateRarity.create(TTTrimPatterns.OCCULT, Rarity.RARE);
            ForgeRegistries.ITEMS.register(id("arcane"), arcane);
            ForgeRegistries.ITEMS.register(id("occult"), occult);
        } finally {
            items.freeze();
            registry.freeze();
        }
    }

    @Test
    void factoryPreservesNativeTooltipSlotsAndAppliesOnlyTheRequestedRarity() {
        var vanilla = (SmithingTemplateItem) Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE;
        for (var template : List.of(arcane, occult)) {
            assertEquals(vanilla.getBaseSlotDescription(), template.getBaseSlotDescription());
            assertEquals(vanilla.getAdditionSlotDescription(), template.getAdditionSlotDescription());
            assertEquals(vanilla.getBaseSlotEmptyIcons(), template.getBaseSlotEmptyIcons());
            assertEquals(vanilla.getAdditionalSlotEmptyIcons(), template.getAdditionalSlotEmptyIcons());
        }
        assertEquals("trim_pattern.thaumaturge.arcane", title(arcane));
        assertEquals("trim_pattern.thaumaturge.occult", title(occult));
        assertEquals(Rarity.COMMON, new ItemStack(vanilla).getRarity());
        assertEquals(Rarity.UNCOMMON, new ItemStack(arcane).getRarity());
        assertEquals(Rarity.RARE, new ItemStack(occult).getRarity());
    }

    @Test
    void nativeRarityStillHandlesEnchantedTemplates() {
        var arcaneStack = new ItemStack(arcane);
        var occultStack = new ItemStack(occult);
        arcaneStack.enchant(Enchantments.UNBREAKING, 1);
        occultStack.enchant(Enchantments.UNBREAKING, 1);
        assertEquals(Rarity.RARE, arcaneStack.getRarity());
        assertEquals(Rarity.EPIC, occultStack.getRarity());
    }

    @Test
    void mergesTheNativeDataDrivenMaterialIndexesByOverridePrecedence() {
        var brass = new TrimItemModel("thaumaturge_brass", 0.031F);
        var crystal = new TrimItemModel("thaumaturge_essentia_crystal", 0.034F);
        assertEquals(List.of(brass, crystal,
                        new TrimItemModel("quartz", 0.1F), new TrimItemModel("iron", 0.2F),
                        new TrimItemModel("netherite", 0.3F), new TrimItemModel("redstone", 0.4F),
                        new TrimItemModel("copper", 0.5F), new TrimItemModel("gold", 0.6F),
                        new TrimItemModel("emerald", 0.7F), new TrimItemModel("diamond", 0.8F),
                        new TrimItemModel("lapis", 0.9F), new TrimItemModel("amethyst", 1.0F)),
                TrimItemModel.withVanillaMaterials(List.of(crystal, brass)));
    }

    private static String title(SmithingTemplateItem item) {
        List<Component> tooltip = new ArrayList<>();
        item.appendHoverText(new ItemStack(item), null, tooltip, TooltipFlag.Default.NORMAL);
        return ((TranslatableContents) tooltip.get(0).getContents()).getKey();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("port_trim_template", path);
    }
}
