package com.leclowndu93150.thaumaturge.content.equipment.trim;

import com.leclowndu93150.thaumaturge.mixin.world.item.ItemRarityAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.armortrim.TrimPattern;

public final class TrimTemplateRarity {
    private TrimTemplateRarity() {}

    public static SmithingTemplateItem create(ResourceKey<TrimPattern> pattern, Rarity rarity) {
        SmithingTemplateItem template = SmithingTemplateItem.createArmorTrimTemplate(pattern);
        ((ItemRarityAccessor) template).thaumaturge$setRarity(rarity);
        return template;
    }
}
