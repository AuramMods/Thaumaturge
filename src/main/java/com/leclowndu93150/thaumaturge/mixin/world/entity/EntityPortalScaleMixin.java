package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityScale;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Sable overwrites the portal entry method at priority 1000.
@Mixin(value = Entity.class, priority = 1100)
abstract class EntityPortalScaleMixin {
    @ModifyExpressionValue(method = "getRelativePortalPosition",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    private EntityDimensions thaumaturge$scalePortalEntryDimensions(EntityDimensions nativeSize) {
        return nativeSize.scale(EntityScale.factor((Entity) (Object) this));
    }
}
