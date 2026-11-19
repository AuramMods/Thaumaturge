package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityScale;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderDispatcher.class)
abstract class EntityShadowScaleMixin {
    @ModifyArg(method = "render", index = 0, at = @At(value = "INVOKE", remap = false,
            target = "Ljava/lang/Math;min(FF)F"))
    private float thaumaturge$scaleShadowRadius(float radius, @Local(argsOnly = true) Entity entity) {
        return radius * EntityScale.factor(entity);
    }
}
