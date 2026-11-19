package com.leclowndu93150.thaumaturge.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** The motion observable by the server for entities and client-controlled players. */
public final class KnownMovement {
    private KnownMovement() {}

    public static Vec3 of(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            Entity vehicle = player.getVehicle();
            if (vehicle != null && vehicle.getControllingPassenger() != player) {
                return of(vehicle);
            }
            return ((Access) player).thaumaturge$getKnownMovement();
        }
        if (entity.getControllingPassenger() instanceof Player controller && entity.isAlive()) {
            return of(controller);
        }
        return entity.getDeltaMovement();
    }

    public interface Access {
        Vec3 thaumaturge$getKnownMovement();

        void thaumaturge$setKnownMovement(Vec3 movement);
    }
}
