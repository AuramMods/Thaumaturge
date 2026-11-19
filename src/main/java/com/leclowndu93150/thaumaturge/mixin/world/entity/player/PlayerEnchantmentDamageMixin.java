package com.leclowndu93150.thaumaturge.mixin.world.entity.player;

import com.leclowndu93150.thaumaturge.content.entity.EntityEnchantmentDamage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerEnchantmentDamageMixin {
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
    private float thaumaturge$damageBonus(ItemStack weapon, MobType nativeType, Operation<Float> original,
            @Local(argsOnly = true) Entity target) {
        return original.call(weapon, nativeType) + EntityEnchantmentDamage.additionalBonus(weapon, nativeType, target.getType());
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostDamageEffects(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;)V"))
    private void thaumaturge$applyDirectBane(Entity target, CallbackInfo callback) {
        EntityEnchantmentDamage.applyBaneOnDirectHit((Player) (Object) this, target);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean thaumaturge$applySweepingBane(LivingEntity target, DamageSource source, float damage, Operation<Boolean> original) {
        boolean hit = original.call(target, source, damage);
        // Sweep post-attack effects run after each secondary damage attempt.
        EntityEnchantmentDamage.applyBaneOnDirectHit((Player) (Object) this, target);
        return hit;
    }
}
