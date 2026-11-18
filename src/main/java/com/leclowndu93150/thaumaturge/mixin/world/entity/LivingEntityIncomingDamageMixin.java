package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.event.combat.IncomingDamageEvent;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityIncomingDamageMixin {
    @Inject(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), cancellable = true)
    private void thaumaturge$incomingDamage(DamageSource source, float originalAmount, CallbackInfoReturnable<Boolean> cir,
            @Local(argsOnly = true) LocalFloatRef amount) {
        var event = new IncomingDamageEvent((LivingEntity) (Object) this, source, originalAmount);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            cir.setReturnValue(false);
        } else {
            amount.set(event.getAmount());
        }
    }
}
