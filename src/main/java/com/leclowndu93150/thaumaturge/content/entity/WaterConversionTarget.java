package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

/** Supplies the actual target type to Forge's inherited zombie conversion event. */
public interface WaterConversionTarget {
    EntityType<? extends Zombie> waterConversionTarget();
}
