package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.shield.AbsorptionCapacity;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityAbsorptionMixin implements AbsorptionCapacity.LoadingAccess {
    @Unique
    private boolean thaumaturge$readingAbsorption;

    @Override
    public boolean thaumaturge$isReadingAbsorption() {
        return thaumaturge$readingAbsorption;
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void thaumaturge$addAbsorptionCapacity(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        var capacity = TTAttributes.MAX_ABSORPTION.orElse(null);
        if (capacity != null) {
            cir.getReturnValue().add(capacity);
        }
    }

    @Redirect(method = "readAdditionalSaveData", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V"))
    private void thaumaturge$readAbsorptionBeforeAttributes(LivingEntity entity, float amount) {
        thaumaturge$readingAbsorption = true;
        try {
            entity.setAbsorptionAmount(amount);
        } finally {
            thaumaturge$readingAbsorption = false;
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void thaumaturge$restorePotionCapacity(CallbackInfo ci) {
        AbsorptionCapacity.restorePotionCapacity((LivingEntity) (Object) this);
    }

    @Inject(method = "onEffectAdded", at = @At("RETURN"))
    private void thaumaturge$grantNewPotionPoints(MobEffectInstance effect, Entity source, CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        if (effect.getEffect() == MobEffects.ABSORPTION && !entity.level().isClientSide()
                && AbsorptionCapacity.attribute(entity) != null) {
            entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(), AbsorptionCapacity.potionPoints(effect.getAmplifier())));
        }
    }

    @Inject(method = {"onEffectUpdated", "onEffectRemoved"}, at = @At("RETURN"))
    private void thaumaturge$reconcileEffectCapacity(CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        if (!entity.level().isClientSide()) {
            AbsorptionCapacity.reconcile(entity);
        }
    }

    @Inject(method = "tickEffects", at = @At("RETURN"))
    private void thaumaturge$reconcileAbsorptionAfterEffects(CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        if (!entity.level().isClientSide()) {
            AbsorptionCapacity.reconcile(entity);
            if (entity.getAbsorptionAmount() <= 0.0F && entity.hasEffect(MobEffects.ABSORPTION)
                    && AbsorptionCapacity.attribute(entity) != null) {
                entity.removeEffect(MobEffects.ABSORPTION);
            }
        }
    }
}
