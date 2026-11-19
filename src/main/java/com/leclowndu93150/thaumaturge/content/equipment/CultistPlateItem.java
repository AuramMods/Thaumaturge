package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityInhabitedZombie;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CultistPlateItem extends ArmorItem {
    private static final ResourceLocation ZOMBIE_PLATE_OUTER =
            TTIds.rl("textures/models/armor/zombie_plate_layer_1.png");
    private static final ResourceLocation ZOMBIE_PLATE_INNER =
            TTIds.rl("textures/models/armor/zombie_plate_layer_2.png");

    public CultistPlateItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
        if (entity instanceof EntityInhabitedZombie) {
            return (slot == EquipmentSlot.LEGS ? ZOMBIE_PLATE_INNER : ZOMBIE_PLATE_OUTER).toString();
        }
        return null;
    }
}
