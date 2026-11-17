package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataPatch;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.thaumaturge.itemdata.EnchantmentData;

/**
 * A single obtain or craft requirement on a {@link IResearchStage}.
 *
 * <p>The requirement matches any item from {@code items}, accepting either a tag reference or
 * one or more direct item references. The player must obtain or craft at least {@code amount}
 * matching items, counted in aggregate across the matched set.
 *
 * <p>{@code components} narrows the match further. Every declared component must be present on
 * the candidate stack and equal to the declared value, and a component declared as removed must
 * be absent. Enchantment components are the one exception: they match as a lower bound, so a
 * requirement for Fortune I is satisfied by any level of Fortune and ignores whatever other
 * enchantments the stack carries.
 *
 * @param items items that satisfy this requirement
 * @param components component values a candidate stack must carry
 * @param amount the minimum aggregate count, must be positive
 * @since 1.0.0
 */
public record ResearchRequirement(HolderSet<Item> items, ItemDataPatch components, int amount) {
    /** Codec for datapack serialization. */
    public static final Codec<ResearchRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM)
                            .fieldOf("items")
                            .forGetter(ResearchRequirement::items),
                    Codecs.optionalFieldOf(ItemDataPatch.CODEC, "components", ItemDataPatch.EMPTY)
                            .forGetter(ResearchRequirement::components),
                    Codec.INT.fieldOf("amount").forGetter(ResearchRequirement::amount))
            .apply(instance, ResearchRequirement::new));

    /**
     * Returns whether the given stack counts towards this requirement.
     *
     * @param stack the candidate stack, may be empty
     * @return {@code true} when the stack's item and components both satisfy the requirement
     */
    public boolean matches(ItemStack stack) {
        return items.contains(stack.getItemHolder()) && matchesComponents(stack);
    }

    private boolean matchesComponents(ItemStack stack) {
        for (Map.Entry<ItemDataKey<?>, Optional<?>> entry : components.entrySet()) {
            ItemDataKey<?> type = entry.getKey();
            Optional<?> required = entry.getValue();
            if (required.isEmpty()) {
                if (ItemData.has(stack, type)) {
                    return false;
                }
                continue;
            }
            Object value = required.get();
            Object present = ItemData.get(stack, type);
            if (value instanceof EnchantmentData enchantments) {
                if (!hasAtLeast(enchantments, present)) {
                    return false;
                }
                continue;
            }
            if (!value.equals(present)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasAtLeast(EnchantmentData required, Object present) {
        EnchantmentData held =
                present instanceof EnchantmentData enchantments ? enchantments : EnchantmentData.EMPTY;
        return held.containsAtLeast(required);
    }
}
