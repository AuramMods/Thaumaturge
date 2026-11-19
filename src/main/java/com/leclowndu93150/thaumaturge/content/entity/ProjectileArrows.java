package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ProjectileArrows {
    private static final int FLAME_SECONDS = 100;

    private ProjectileArrows() {}

    public static Arrow create(Level level, LivingEntity shooter, ItemStack ammunition,
            @Nullable ItemStack bow, double baseDamage) {
        var arrow = new Arrow(level, shooter);
        arrow.setEffectsFromItem(ammunition);
        if (bow != null && level instanceof ServerLevel) {
            // In the source release Power adds half a point per level before velocity is applied.
            baseDamage = (float) baseDamage + 0.5F * bow.getEnchantmentLevel(Enchantments.POWER_ARROWS);
            arrow.setKnockback(bow.getEnchantmentLevel(Enchantments.PUNCH_ARROWS));
            arrow.setPierceLevel((byte) bow.getEnchantmentLevel(Enchantments.PIERCING));
            if (bow.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                arrow.setSecondsOnFire(FLAME_SECONDS);
            }
            ProjectileLooting.remember(arrow, ProjectileLooting.levelOn(bow));
        }
        arrow.setBaseDamage(baseDamage);
        return arrow;
    }
}
