package com.leclowndu93150.thaumaturge.mixin.world.effect;

import com.leclowndu93150.thaumaturge.content.entity.shield.AbsorptionCapacity;
import net.minecraft.world.effect.AbsoptionMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbsoptionMobEffect.class)
abstract class AbsorptionMobEffectMixin extends MobEffect {
    protected AbsorptionMobEffectMixin(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Inject(method = "addAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$grantPotionCapacity(LivingEntity entity, AttributeMap attributes, int amplifier, CallbackInfo ci) {
        var capacity = AbsorptionCapacity.attribute(entity);
        if (capacity != null) {
            super.addAttributeModifiers(entity, attributes, amplifier);
            AbsorptionCapacity.addPotionCapacity(capacity, amplifier);
            ci.cancel();
        }
    }

    @Inject(method = "removeAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$removePotionCapacity(LivingEntity entity, AttributeMap attributes, int amplifier, CallbackInfo ci) {
        var capacity = AbsorptionCapacity.attribute(entity);
        if (capacity != null) {
            AbsorptionCapacity.removePotionCapacity(capacity);
            super.removeAttributeModifiers(entity, attributes, amplifier);
            ci.cancel();
        }
    }
}
