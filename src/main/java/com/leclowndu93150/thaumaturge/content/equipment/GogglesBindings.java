package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

public final class GogglesBindings implements GogglesAccess.Bindings {
    @Override
    public Holder<Attribute> visDiscount() {
        return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(TTAttributes.VIS_DISCOUNT.get());
    }

    @Override
    public boolean isRevealing(ItemStack stack) {
        return ItemData.has(stack, TTDataComponents.GOGGLES_UPGRADE.get());
    }
}
