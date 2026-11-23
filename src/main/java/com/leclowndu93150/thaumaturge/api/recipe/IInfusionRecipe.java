package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectRequirements;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.RecipeMatcher;

/**
 * An infusion altar recipe. The central pedestal holds the catalyst; the
 * surrounding pedestals hold the components, consumed one at a time while
 * essentia drains from nearby containers.
 *
 * @since 1.0
 */
public interface IInfusionRecipe extends ResearchGated {
    /**
     * @return the ingredient required on the central pedestal
     */
    Ingredient catalyst();

    /**
     * @return the component ingredients, each consumed from a surrounding pedestal
     */
    List<Ingredient> components();

    /**
     * @return the essentia drained over the course of the craft, before cost modifiers
     */
    AspectList aspects();

    /** The full essentia cost, including any aspect tag alternatives. */
    default AspectRequirements aspectRequirements() {
        return AspectRequirements.fixed(aspects());
    }

    /**
     * @return the base instability of the craft; higher values destabilize the matrix faster
     */
    int instability();

    /**
     * @return a copy of the crafting result
     */
    ItemStack resultItem();

    /**
     * Matches the available pedestal stacks against {@link #components()}, one stack per
     * component, order-insensitive.
     *
     * @param available the stacks present on the surrounding pedestals
     * @return single-count copies of the matched stacks in component order, or {@code null}
     *         when the stacks do not satisfy the components exactly
     */
    default List<ItemStack> matchComponents(List<ItemStack> available) {
        return matchComponents(available, components());
    }

    static List<ItemStack> matchComponents(List<ItemStack> available, List<Ingredient> needed) {
        int[] matches = RecipeMatcher.findMatches(available, needed);
        if (matches == null) {
            return null;
        }
        List<ItemStack> consumed = new ArrayList<>(Collections.nCopies(matches.length, ItemStack.EMPTY));
        for (int slot = 0; slot < matches.length; slot++) {
            consumed.set(matches[slot], available.get(slot).copyWithCount(1));
        }
        return consumed;
    }
}
