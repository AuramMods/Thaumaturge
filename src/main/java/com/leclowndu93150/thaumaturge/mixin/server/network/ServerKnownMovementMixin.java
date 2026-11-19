package com.leclowndu93150.thaumaturge.mixin.server.network;

import com.leclowndu93150.thaumaturge.compat.KnownMovement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerKnownMovementMixin {
    @WrapOperation(method = "handleMovePlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setOnGroundWithKnownMovement(ZLnet/minecraft/world/phys/Vec3;)V"))
    private void thaumaturge$recordPlayerMovement(ServerPlayer player, boolean onGround, Vec3 movement,
            Operation<Void> original) {
        original.call(player, onGround, movement);
        ((KnownMovement.Access) player).thaumaturge$setKnownMovement(movement);
    }

    @WrapOperation(method = "handleMoveVehicle", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;checkMovementStatistics(DDD)V"))
    private void thaumaturge$recordVehicleMovement(ServerPlayer player, double x, double y, double z,
            Operation<Void> original, @Local Entity vehicle,
            @Local(index = 4) double previousX, @Local(index = 6) double previousY, @Local(index = 8) double previousZ) {
        // The Forge statistics call uses the rider's current position. 1.21 tracks the vehicle's accepted position.
        ((KnownMovement.Access) player).thaumaturge$setKnownMovement(
                vehicle.position().subtract(previousX, previousY, previousZ));
        original.call(player, x, y, z);
    }
}
