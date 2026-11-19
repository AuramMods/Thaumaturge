package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

/** Resolves Looting from the item that produced the hit. */
public final class ProjectileLooting {
    private static final String SHOT_LOOTING = "thaumaturge:shot_looting";

    public interface ThrownWeapon {
        int thaumaturge$weaponLooting();
    }

    private ProjectileLooting() {}

    public static int levelOn(ItemStack weapon) {
        return weapon.isEmpty() ? 0 : weapon.getEnchantmentLevel(Enchantments.MOB_LOOTING);
    }

    public static void remember(AbstractArrow arrow, int level) {
        if (level > 0) {
            arrow.getPersistentData().putInt(SHOT_LOOTING, level);
        }
    }

    public static int levelFor(DamageSource source) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof LivingEntity attacker) {
            return levelOn(attacker.getMainHandItem());
        }
        if (direct instanceof ThrownWeapon thrown) {
            return thrown.thaumaturge$weaponLooting();
        }
        if (direct instanceof AbstractArrow arrow) {
            return arrow.getPersistentData().getInt(SHOT_LOOTING);
        }
        return 0;
    }
}
