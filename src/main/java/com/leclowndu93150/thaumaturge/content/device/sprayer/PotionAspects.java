package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import com.leclowndu93150.thaumaturge.itemdata.PotionData;

final class PotionAspects {
    private static final int REDUCTION_PERCENT = 66;
    private static final int PERCENT = 100;
    private static final int ALKIMIA_PER_REAGENT = 3;
    private static final int FALLBACK_AMOUNT = 5;
    private static final int ASPECT_CAP = 10;

    private PotionAspects() {}

    static AspectList of(ServerLevel level, ItemStack stack) {
        Optional<Holder<Potion>> potion = ItemData.getOrDefault(stack, VanillaItemData.POTION_CONTENTS, PotionData.EMPTY)
                .potion();
        List<ItemStack> reagents = potion.isPresent() ? BrewingRoutes.reagents(potion.get()) : List.of();
        if (reagents.isEmpty()) {
            return fallback(level);
        }
        return trimToCap(reduce(total(level, reagents)));
    }

    private static AspectList fallback(ServerLevel level) {
        return AspectList.EMPTY
                .add(aspect(level, TTAspects.PRAECANTATIO), FALLBACK_AMOUNT)
                .add(aspect(level, TTAspects.ALKIMIA), FALLBACK_AMOUNT);
    }

    private static Holder<IAspect> aspect(ServerLevel level, ResourceKey<IAspect> key) {
        Holder<IAspect> holder = Aspects.resolve(level, key);
        if (holder == null) {
            throw new IllegalStateException("Aspect " + key.location() + " is not registered");
        }
        return holder;
    }

    private static AspectList total(ServerLevel level, List<ItemStack> reagents) {
        Holder<IAspect> alkimia = aspect(level, TTAspects.ALKIMIA);
        AspectList sum = AspectList.EMPTY;
        for (ItemStack reagent : reagents) {
            sum = sum.add(AspectIndexAccess.of(reagent)).add(alkimia, ALKIMIA_PER_REAGENT);
        }
        return sum;
    }

    private static AspectList reduce(AspectList sum) {
        AspectList reduced = AspectList.EMPTY;
        for (AspectInstance entry : sum.entries()) {
            int remaining = entry.amount() - entry.amount() * REDUCTION_PERCENT / PERCENT;
            if (remaining > 0) {
                reduced = reduced.add(entry.aspect(), remaining);
            }
        }
        return reduced;
    }

    private static AspectList trimToCap(AspectList list) {
        AspectList trimmed = list;
        while (trimmed.size() > ASPECT_CAP) {
            AspectInstance weakest = trimmed.entries().get(0);
            for (AspectInstance entry : trimmed.entries()) {
                if (entry.amount() < weakest.amount()) {
                    weakest = entry;
                }
            }
            trimmed = trimmed.without(weakest.aspect());
        }
        return trimmed;
    }

}
