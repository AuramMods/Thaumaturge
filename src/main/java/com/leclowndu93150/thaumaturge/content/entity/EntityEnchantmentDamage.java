package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class EntityEnchantmentDamage {
    private EntityEnchantmentDamage() {}

    /** Adds the tag-based bonuses that the native MobType calculation did not include. */
    public static float additionalBonus(ItemStack weapon, MobType nativeType, EntityType<?> target) {
        float bonus = 0.0F;
        if (nativeType != MobType.UNDEAD) {
            int smite = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, weapon);
            if (smite > 0 && target.is(TTEntityTags.SENSITIVE_TO_SMITE)) {
                bonus += Enchantments.SMITE.getDamageBonus(smite, MobType.UNDEAD, weapon);
            }
        }
        if (nativeType != MobType.ARTHROPOD) {
            int bane = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, weapon);
            if (bane > 0 && target.is(TTEntityTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
                bonus += Enchantments.BANE_OF_ARTHROPODS.getDamageBonus(bane, MobType.ARTHROPOD, weapon);
            }
        }
        return bonus;
    }

    public static void applyBaneOnDirectHit(LivingEntity attacker, Entity target) {
        if (!(attacker.level() instanceof ServerLevel) || !(target instanceof LivingEntity living)) {
            return;
        }
        int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, attacker.getMainHandItem());
        if (level > 0 && living.getType().is(TTEntityTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            float maximumSeconds = 1.5F + 0.5F * (level - 1);
            int duration = Math.round(Mth.nextFloat(living.getRandom(), 1.5F, maximumSeconds) * 20.0F);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 3));
        }
    }
}
