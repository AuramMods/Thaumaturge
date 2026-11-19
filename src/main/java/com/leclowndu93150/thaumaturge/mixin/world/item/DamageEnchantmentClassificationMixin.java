package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DamageEnchantment.class)
abstract class DamageEnchantmentClassificationMixin {
    @ModifyExpressionValue(method = "doPostAttack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getMobType()Lnet/minecraft/world/entity/MobType;"))
    private MobType thaumaturge$baneSlownessClassification(MobType nativeType, @Local(argsOnly = true) Entity target) {
        return target.getType().is(TTEntityTags.SENSITIVE_TO_BANE_OF_ARTHROPODS) ? MobType.ARTHROPOD : nativeType;
    }
}
