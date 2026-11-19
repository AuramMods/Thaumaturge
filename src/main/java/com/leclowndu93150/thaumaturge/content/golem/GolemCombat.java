package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.content.entity.ProjectileLooting;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

final class GolemCombat {
    private static final float LOOT_CHANCE = 0.3F;
    private static final float LOOTING_BONUS_PER_LEVEL = 0.15F;

    private GolemCombat() {}

    static boolean tryMeleeHit(Mob attacker, Entity target) {
        if (!(attacker.level() instanceof ServerLevel)) {
            return false;
        }
        DamageSource source = attacker.damageSources().mobAttack(attacker);
        ItemStack weapon = attacker.getMainHandItem();
        MobType targetType = target instanceof LivingEntity living ? living.getMobType() : MobType.UNDEFINED;
        float damage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE)
                + EnchantmentHelper.getDamageBonus(weapon, targetType);
        if (!target.hurt(source, damage)) {
            return false;
        }
        float knockback = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK)
                + EnchantmentHelper.getKnockbackBonus(attacker);
        if (knockback > 0.0F && target instanceof LivingEntity living) {
            living.knockback(knockback * 0.5F,
                    Mth.sin(attacker.getYRot() * Mth.DEG_TO_RAD), -Mth.cos(attacker.getYRot() * Mth.DEG_TO_RAD));
            attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        }
        attacker.doEnchantDamageEffects(attacker, target);
        int fireAspect = EnchantmentHelper.getFireAspect(attacker);
        if (fireAspect > 0) {
            target.setSecondsOnFire(fireAspect * 4);
        }
        attacker.setLastHurtMob(target);
        return true;
    }

    static void rollComponentDrops(
            Iterable<ItemStack> components, DamageSource source, RandomSource random, Consumer<ItemStack> drop) {
        float chance = LOOT_CHANCE + LOOTING_BONUS_PER_LEVEL * ProjectileLooting.levelFor(source);
        for (ItemStack stack : components) {
            ItemStack copy = stack.copy();
            if (random.nextFloat() < chance) {
                if (!copy.isEmpty()) {
                    copy.shrink(random.nextInt(copy.getCount()));
                }
                drop.accept(copy);
            }
        }
    }
}
