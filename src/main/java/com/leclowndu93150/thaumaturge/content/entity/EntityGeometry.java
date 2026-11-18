package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Zombie;

/** The source eye height and spawn clearance carried by a native entity type. */
public record EntityGeometry(float eyeHeight, float spawnScale) {
    public interface Access {
        EntityGeometry thaumaturge$getGeometry();

        void thaumaturge$setGeometry(EntityGeometry geometry);
    }

    public static <T extends Entity> EntityType<T> attach(EntityType<T> type, float eyeHeight, float spawnScale) {
        ((Access) type).thaumaturge$setGeometry(new EntityGeometry(eyeHeight, spawnScale));
        return type;
    }

    public static EntityGeometry of(EntityType<?> type) {
        return ((Access) type).thaumaturge$getGeometry();
    }

    public float eyeHeight(Entity entity, Pose pose, EntityDimensions dimensions, float nativeHeight) {
        if ((entity instanceof LivingEntity && pose == Pose.SLEEPING) || (entity instanceof Zombie zombie && zombie.isBaby())) {
            return nativeHeight;
        }
        float standingHeight = entity.getType().getHeight();
        return standingHeight > 0.0F ? eyeHeight * (dimensions.height / standingHeight) : eyeHeight;
    }
}
