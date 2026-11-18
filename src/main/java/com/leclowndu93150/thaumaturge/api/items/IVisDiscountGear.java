package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

/**
 * Marker for worn gear that reduces vis cost for thaumaturgic actions.
 *
 * @since 1.0.0
 */
public interface IVisDiscountGear {
    /**
     * Returns the percentage discount applied to vis-consuming casts when the player wears
     * this item. The Goggles of Revealing implementation returns {@code 5}.
     *
     * @param stack  the worn stack
     * @return the discount in whole percent units, never negative
     */
    int getVisDiscount(ItemStack stack);

    /**
     * Returns whether the vis modifier applies in the requested equipment slot.
     * Items without an equipable slot apply to every native equipment slot.
     */
    default boolean appliesInSlot(ItemStack stack, EquipmentSlot slot) {
        return !(stack.getItem() instanceof Equipable equipable) || slot == equipable.getEquipmentSlot();
    }
}
