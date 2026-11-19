package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

public final class EntityIgnition {
    private EntityIgnition() {}

    public static void igniteForSeconds(Entity entity, float seconds) {
        int ticks = Mth.floor(seconds * 20.0F);
        if (entity instanceof LivingEntity living) {
            ticks = ProtectionEnchantment.getFireAfterDampener(living, ticks);
        }
        if (entity.getRemainingFireTicks() < ticks) {
            entity.setRemainingFireTicks(ticks);
        }
    }
}
