package com.leclowndu93150.thaumaturge.content.essentia.item;

import com.leclowndu93150.thaumaturge.compat.Java17;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.function.Function;
import java.util.function.Supplier;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataKey;
import net.minecraft.world.item.ItemStack;

public record ComponentEssentia<T>(
        ItemStack stack,
        Supplier<ItemDataKey<T>> type,
        Function<T, AspectList> reader,
        Function<AspectList, T> writer)
        implements IItemEssentia {
    public static ComponentEssentia<EssentiaList> jar(ItemStack stack) {
        return new ComponentEssentia<>(
                stack, TTDataComponents.ESSENTIA_CONTENTS, EssentiaList::contents, EssentiaList::new);
    }

    public static ComponentEssentia<AspectList> phial(ItemStack stack) {
        return new ComponentEssentia<>(stack, TTDataComponents.ASPECTS, Function.identity(), Function.identity());
    }

    public static ComponentEssentia<AspectInstance> crystal(ItemStack stack) {
        return new ComponentEssentia<>(
                stack,
                TTDataComponents.CRYSTAL_ASPECT,
                AspectList::of,
                aspects -> Java17.first(aspects.entries()).withAmount(1));
    }

    @Override
    public AspectList getAspects() {
        T stored = ItemData.get(stack, type.get());
        return stored == null ? AspectList.EMPTY : reader.apply(stored);
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects.isEmpty()) {
            ItemData.remove(stack, type.get());
        } else {
            ItemData.set(stack, type.get(), writer.apply(aspects));
        }
    }
}
