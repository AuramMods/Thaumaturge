package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.event.combat.DamagePhaseEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
abstract class LivingEntityDamagePhaseMixin {
    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/entity/LivingEntity;getAbsorptionAmount()F"))
    private void thaumaturge$beforeAbsorption(DamageSource source, float damage, CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        MinecraftForge.EVENT_BUS.post(new DamagePhaseEvent.BeforeAbsorption(entity, source, damage));
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void thaumaturge$afterAbsorbedDamage(DamageSource source, float damage, CallbackInfo ci, float healthDamage) {
        if (healthDamage == 0.0F) {
            var entity = (LivingEntity) (Object) this;
            MinecraftForge.EVENT_BUS.post(new DamagePhaseEvent.AfterDamage(entity, source, healthDamage));
        }
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void thaumaturge$afterHealthDamage(DamageSource source, float damage, CallbackInfo ci, float healthDamage) {
        var entity = (LivingEntity) (Object) this;
        MinecraftForge.EVENT_BUS.post(new DamagePhaseEvent.AfterDamage(entity, source, healthDamage));
    }
}
