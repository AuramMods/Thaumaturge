package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityEnchantmentDamage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityBaneSlownessMixin {
    @Inject(method = "doEnchantDamageEffects", at = @At("TAIL"))
    private void thaumaturge$applyDirectBane(LivingEntity attacker, Entity target, CallbackInfo callback) {
        // Projectile callers pass their owner as the attacker while the projectile is the receiver.
        if ((Object) this == attacker) {
            EntityEnchantmentDamage.applyBaneOnDirectHit(attacker, target);
        }
    }
}
