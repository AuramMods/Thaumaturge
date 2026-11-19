package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;

public class DyeableModeledArmorItem extends ModeledArmorItem implements DyeableLeatherItem {
    public DyeableModeledArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }
}
