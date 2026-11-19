package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityEnchantmentDamage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
abstract class MobEnchantmentDamageMixin {
    @WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"))
    private float thaumaturge$damageBonus(ItemStack weapon, MobType nativeType, Operation<Float> original,
            @Local(argsOnly = true) Entity target) {
        return original.call(weapon, nativeType) + EntityEnchantmentDamage.additionalBonus(weapon, nativeType, target.getType());
    }
}
