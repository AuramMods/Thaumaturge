package com.leclowndu93150.thaumaturge.mixin.world.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageEnchantment.class)
abstract class DamageEnchantmentClassificationMixin {
    @Inject(method = "doPostAttack", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$deferBaneToDirectHits(LivingEntity attacker, Entity target, int level, CallbackInfo callback) {
        if ((Object) this == Enchantments.BANE_OF_ARTHROPODS) {
            callback.cancel();
        }
    }
}
