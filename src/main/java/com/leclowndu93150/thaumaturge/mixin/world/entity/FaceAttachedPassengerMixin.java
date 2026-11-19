package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.FaceAttachedPassenger;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
abstract class FaceAttachedPassengerMixin {
    @WrapOperation(method = "positionRider(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V"))
    private void thaumaturge$positionFacePassenger(Entity vehicle, Entity passenger, Entity.MoveFunction move,
            Operation<Void> original) {
        if (vehicle instanceof LivingEntity host && passenger instanceof FaceAttachedPassenger
                && host.hasPassenger(passenger)) {
            double reach = host.getBbWidth() * 0.5 + passenger.getBbWidth() * 0.25;
            Vec3 face = host.getEyePosition().add(host.getLookAngle().scale(reach))
                    .add(0.0, -passenger.getBbHeight() * 0.5, 0.0);
            Entity.MoveFunction moveToFace = (rider, x, y, z) -> move.accept(rider, face.x, face.y, face.z);
            original.call(vehicle, passenger, moveToFace);
        } else {
            original.call(vehicle, passenger, move);
        }
    }
}
