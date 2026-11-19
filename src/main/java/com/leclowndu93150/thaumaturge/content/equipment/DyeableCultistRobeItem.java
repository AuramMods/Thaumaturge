package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;

public final class DyeableCultistRobeItem extends CultistRobeItem implements DyeableLeatherItem {
    public DyeableCultistRobeItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }
}
