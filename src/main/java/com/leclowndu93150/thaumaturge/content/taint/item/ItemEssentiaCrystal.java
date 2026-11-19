package com.leclowndu93150.thaumaturge.content.taint.item;

import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.content.essentia.item.ComponentEssentia;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jspecify.annotations.Nullable;

public final class ItemEssentiaCrystal extends Item {
    public ItemEssentiaCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return EssentiaCapabilities.item(() -> ComponentEssentia.crystal(stack), null);
    }

    @Override
    public Component getName(ItemStack stack) {
        Holder<IAspect> aspect = aspectOf(stack);
        if (aspect == null) {
            return Component.translatable("item.thaumaturge.essentia_crystal.unknown");
        }
        return Component.translatable("item.thaumaturge.essentia_crystal", AspectComponents.name(aspect));
    }

    public static Holder<IAspect> aspectOf(ItemStack stack) {
        AspectInstance instance = ItemData.get(stack, TTDataComponents.CRYSTAL_ASPECT.get());
        return instance == null ? null : instance.aspect();
    }

    public static int colorOf(ItemStack stack) {
        Holder<IAspect> aspect = aspectOf(stack);
        return aspect == null ? 0xFFFFFF : aspect.value().color();
    }
}
