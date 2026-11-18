package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingAccessor;
import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingMixAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

/** The sprayer's source route walk over Forge's actual static vanilla brewing table. */
final class BrewingRoutes {
    private static final int MAX_SEARCH_LEVEL = 16;
    private BrewingRoutes() {}

    static List<ItemStack> reagents(Holder<Potion> target) {
        return reagents(PotionBrewingAccessor.thaumaturge$getPotionMixes(), target);
    }

    static List<ItemStack> reagents(List<?> mixes, Holder<Potion> target) {
        Map<Item, ItemStack> reagents = new LinkedHashMap<>();
        collectReagents(mixes, target, 0, new HashSet<>(), reagents);
        return new ArrayList<>(reagents.values());
    }

    private static void collectReagents(
            List<?> mixes, Holder<?> potion, int level, Set<ResourceKey<?>> searched, Map<Item, ItemStack> reagents) {
        if (level > MAX_SEARCH_LEVEL) return;
        Optional<? extends ResourceKey<?>> key = potion.unwrapKey();
        if (key.isEmpty() || !searched.add(key.get())) return;
        for (Object raw : mixes) {
            PotionBrewingMixAccessor mix = (PotionBrewingMixAccessor) raw;
            if (mix.thaumaturge$getTo().unwrapKey().filter(key.get()::equals).isEmpty()) continue;
            reagentItem(mix).ifPresent(item -> reagents.putIfAbsent(item, new ItemStack(item)));
            collectReagents(mixes, mix.thaumaturge$getFrom(), level + 1, searched, reagents);
        }
    }

    private static Optional<Item> reagentItem(PotionBrewingMixAccessor mix) {
        return Arrays.stream(mix.thaumaturge$getIngredient().getItems()).map(ItemStack::getItem).findFirst();
    }
}
