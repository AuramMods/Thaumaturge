package com.leclowndu93150.thaumaturge.mixin.world.level;

import com.leclowndu93150.thaumaturge.content.entity.EntityScale;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PortalShape.class)
abstract class PortalEntityScaleMixin {
    @ModifyExpressionValue(method = "createPortalInfo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    private static EntityDimensions thaumaturge$useScaledExitClearance(
            EntityDimensions nativeSize, @Local(argsOnly = true) Entity entity) {
        return nativeSize.scale(EntityScale.factor(entity));
    }
}
