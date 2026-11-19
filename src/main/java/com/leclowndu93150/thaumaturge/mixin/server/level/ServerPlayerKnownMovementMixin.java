package com.leclowndu93150.thaumaturge.mixin.server.level;

import com.leclowndu93150.thaumaturge.compat.KnownMovement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerKnownMovementMixin implements KnownMovement.Access {
    @Unique private Vec3 thaumaturge$knownMovement = Vec3.ZERO;

    @Override
    public Vec3 thaumaturge$getKnownMovement() {
        return thaumaturge$knownMovement;
    }

    @Override
    public void thaumaturge$setKnownMovement(Vec3 movement) {
        thaumaturge$knownMovement = movement;
    }
}
