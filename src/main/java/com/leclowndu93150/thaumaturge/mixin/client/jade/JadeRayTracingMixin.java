package com.leclowndu93150.thaumaturge.mixin.client.jade;

import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "snownee.jade.overlay.RayTracing", remap = false)
abstract class JadeRayTracingMixin {
    // Jade builds a ray in camera space. Keep the actual hit in plot coordinates for block lookup.
    @ModifyExpressionValue(method = "rayTrace", at = {
        @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getLocation()Lnet/minecraft/world/phys/Vec3;", remap = true),
        @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getLocation()Lnet/minecraft/world/phys/Vec3;", remap = true),
        @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/EntityHitResult;getLocation()Lnet/minecraft/world/phys/Vec3;", remap = true)
    })
    private Vec3 thaumaturge$useWorldHitPosition(Vec3 position, Entity viewer) {
        return ClientSpaces.renderPosition(viewer.level(), position);
    }
}
