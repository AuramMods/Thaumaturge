package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityGeometry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class, LivingEntity.class})
abstract class EntityEyeHeightMixin {
    @Inject(method = "getEyeHeight(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F",
            at = @At("RETURN"), cancellable = true)
    private void thaumaturge$useAuthoredEyeHeight(Pose pose, EntityDimensions dimensions,
                                                CallbackInfoReturnable<Float> cir) {
        var entity = (Entity) (Object) this;
        EntityGeometry geometry = EntityGeometry.of(entity.getType());
        if (geometry != null) {
            cir.setReturnValue(geometry.eyeHeight(entity, pose, dimensions, cir.getReturnValueF()));
        }
    }
}
