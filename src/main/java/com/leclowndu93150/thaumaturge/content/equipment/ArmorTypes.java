package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.world.item.ArmorItem;

/** Source 1.21.1 per-item armor durability for the four native player armor types. */
public final class ArmorTypes {
    private ArmorTypes() {}
    public static int durability(ArmorItem.Type type, int multiplier) {
        return switch (type) {
            case HELMET -> 11 * multiplier;
            case CHESTPLATE -> 16 * multiplier;
            case LEGGINGS -> 15 * multiplier;
            case BOOTS -> 13 * multiplier;
        };
    }
}
