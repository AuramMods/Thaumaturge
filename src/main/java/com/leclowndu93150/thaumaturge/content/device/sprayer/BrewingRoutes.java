package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.PotionData;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import org.jspecify.annotations.Nullable;

/** Walks the live vanilla and Forge brewing recipes that provide a sprayer's reagent cost. */
final class BrewingRoutes {
    private static final int MAX_SEARCH_LEVEL = 16;
    private BrewingRoutes() {}

    static List<ItemStack> reagents(Holder<Potion> target) {
        return reagents(PotionBrewingAccessor.thaumaturge$getPotionMixes(), target);
    }

    static List<ItemStack> reagents(ItemStack target) {
        Map<Item, ItemStack> reagents = new LinkedHashMap<>();
        collectStack(target, 0, new HashSet<>(), new HashSet<>(), reagents);
        return new ArrayList<>(reagents.values());
    }

    private static void collectStack(ItemStack stack, int depth, Set<PotionVariant> seenStacks,
            Set<ResourceKey<?>> seenVanilla, Map<Item, ItemStack> reagents) {
        if (depth > MAX_SEARCH_LEVEL || !seenStacks.add(new PotionVariant(stack.getItem(), stack.getTag()))) return;
        var potion = ItemData.getOrDefault(stack, VanillaItemData.POTION_CONTENTS, PotionData.EMPTY).potion();
        potion.ifPresent(holder -> collectReagents(
                PotionBrewingAccessor.thaumaturge$getPotionMixes(), holder, depth, seenVanilla, reagents));
        for (var registered : BrewingRecipeRegistry.getRecipes()) {
            if (!(registered instanceof BrewingRecipe recipe)
                    || !ItemStack.isSameItemSameTags(stack, recipe.getOutput())) continue;
            Arrays.stream(recipe.getIngredient().getItems()).findFirst()
                    .ifPresent(example -> reagents.putIfAbsent(example.getItem(), example.copyWithCount(1)));
            Arrays.stream(recipe.getInput().getItems()).filter(input -> input.getItem() instanceof PotionItem).findFirst()
                    .ifPresent(input -> collectStack(input, depth + 1, seenStacks, seenVanilla, reagents));
        }
    }

    private record PotionVariant(Item item, @Nullable CompoundTag tag) {}

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
