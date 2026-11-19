package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityScale;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.event.entity.EntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityScaleMixin {
    @Shadow private EntityDimensions dimensions;
    @Shadow private float eyeHeight;

    @WrapOperation(method = "refreshDimensions", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraftforge/event/ForgeEventFactory;getEntitySizeForge(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;Lnet/minecraft/world/entity/EntityDimensions;F)Lnet/minecraftforge/event/entity/EntityEvent$Size;"))
    private EntityEvent.Size thaumaturge$scaleSizeBeforeForgeEvent(Entity entity, Pose pose,
            EntityDimensions oldSize, EntityDimensions nativeSize, float nativeEyeHeight,
            Operation<EntityEvent.Size> original) {
        float scale = nativeSize.fixed ? 1.0F : EntityScale.factor(entity);
        return original.call(entity, pose, oldSize, nativeSize.scale(scale), nativeEyeHeight * scale);
    }

    @Inject(method = "fixupDimensions", at = @At("RETURN"))
    private void thaumaturge$scaleFixedUpDimensions(CallbackInfo ci) {
        if (!dimensions.fixed) {
            float scale = EntityScale.factor((Entity) (Object) this);
            dimensions = dimensions.scale(scale);
            eyeHeight *= scale;
        }
    }

    @ModifyExpressionValue(method = {"getBoundingBoxForPose", "getDimensionsForge"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    private EntityDimensions thaumaturge$scalePoseDimensions(EntityDimensions nativeSize) {
        return nativeSize.scale(EntityScale.factor((Entity) (Object) this));
    }

    @WrapOperation(method = "getEyeHeight(Lnet/minecraft/world/entity/Pose;)F",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getEyeHeight(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F"))
    private float thaumaturge$scaleQueriedEyeHeight(Entity entity, Pose pose, EntityDimensions nativeSize,
            Operation<Float> original) {
        float nativeEyeHeight = original.call(entity, pose, nativeSize);
        return nativeSize.fixed ? nativeEyeHeight : nativeEyeHeight * EntityScale.factor(entity);
    }
}
