package com.leclowndu93150.thaumaturge.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.AABB;

public final class BlockCollisionChecks {
    private BlockCollisionChecks() {}

    public static boolean hasNoBlockCollision(CollisionGetter level, Entity entity, AABB bounds) {
        for (var shape : level.getBlockCollisions(entity, bounds)) {
            if (!shape.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
