package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import net.minecraft.core.HolderSet;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class ItemRequirementWidgetTest {
    @Test
    void requirementIconKeepsRequiredComponents() {
        Component name = Component.literal("Required variant");
        ResearchRequirement requirement = new ResearchRequirement(
                HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.STONE)),
                ItemDataPatch.builder().set(VanillaItemData.CUSTOM_NAME, name)
                        .build(),
                1);

        ItemStack displayed = ItemRequirementWidget.cycleItemStack(requirement, 0);

        assertEquals(name, ItemData.get(displayed, VanillaItemData.CUSTOM_NAME));
        assertTrue(requirement.matches(displayed));
    }

    @Test
    void wildcardDamageRequirementDoesNotAnimateItsDurability() {
        ItemDataPatch components =
                ItemDataPatch.builder().set(VanillaItemData.DAMAGE, 32767).build();
        ResearchRequirement requirement = new ResearchRequirement(
                HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.WOODEN_SWORD)), components, 1);
        ItemStack expected = new ItemStack(Items.WOODEN_SWORD);
        components.apply(expected);

        ItemStack displayed = ItemRequirementWidget.cycleItemStack(requirement, 0);

        assertEquals(expected.getDamageValue(), displayed.getDamageValue());
    }
}
