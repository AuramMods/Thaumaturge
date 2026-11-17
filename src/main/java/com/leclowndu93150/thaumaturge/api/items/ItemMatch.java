package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.ItemNbt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Immutable rules for matching item stacks, excluding their counts. */
public record ItemMatch(
        boolean anyDamage, boolean anyComponents, boolean componentSubset, boolean sharedTag, boolean sameMod) {
    public static final ItemMatch EXACT = new ItemMatch(false, false, false, false, false);
    public static final ItemMatch ITEM_OR_TAG = EXACT.byTag(true);

    public ItemMatch ignoringDamage(boolean ignore) {
        return new ItemMatch(ignore, anyComponents, componentSubset, sharedTag, sameMod);
    }

    public ItemMatch ignoringComponents(boolean ignore) {
        return new ItemMatch(anyDamage, ignore, componentSubset, sharedTag, sameMod);
    }

    public ItemMatch withComponentSubset(boolean subset) {
        return new ItemMatch(anyDamage, anyComponents, subset, sharedTag, sameMod);
    }

    public ItemMatch byTag(boolean enabled) {
        return new ItemMatch(anyDamage, anyComponents, componentSubset, enabled, sameMod);
    }

    public ItemMatch byMod(boolean enabled) {
        return new ItemMatch(anyDamage, anyComponents, componentSubset, sharedTag, enabled);
    }

    /** Tests candidate against the reference; tag and component-subset matching are directional. */
    public boolean test(ItemStack reference, ItemStack candidate) {
        if (reference.isEmpty() || candidate.isEmpty()) {
            return reference.isEmpty() && candidate.isEmpty();
        }
        if (sameMod && namespace(reference).equals(namespace(candidate))) {
            return true;
        }
        if (sharedTag
                && (reference.getItem() == candidate.getItem()
                        || reference.getTags().anyMatch(candidate::is))) {
            return true;
        }
        if (reference.getItem() != candidate.getItem()) {
            return false;
        }
        if (!anyDamage && reference.getDamageValue() != candidate.getDamageValue()) {
            return false;
        }
        if (anyComponents) {
            return true;
        }
        return reference.areCapsCompatible(candidate)
                && ItemNbt.matches(ItemData.comparisonFields(reference), ItemData.comparisonFields(candidate),
                        anyDamage, componentSubset);
    }

    private static String namespace(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }
}
