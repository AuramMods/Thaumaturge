package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityScale;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityScaleMixin {
    @Unique private float thaumaturge$appliedScale = 1.0F;

    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumaturge$refreshChangedScale(CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        float scale = EntityScale.factor(entity);
        if (scale != thaumaturge$appliedScale) {
            thaumaturge$appliedScale = scale;
            entity.refreshDimensions();
        }
    }

    @ModifyExpressionValue(method = "getLocalBoundsForPose", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    private EntityDimensions thaumaturge$scaleLocalBounds(EntityDimensions nativeSize) {
        return nativeSize.scale(EntityScale.factor((LivingEntity) (Object) this));
    }
}
