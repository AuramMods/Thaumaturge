package com.leclowndu93150.thaumaturge.content.world.space;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface SpaceFrame {
    boolean isWorld();

    Vec3 toWorld(Vec3 local);

    Vec3 toLocal(Vec3 world);

    Vec3 directionToWorld(Vec3 local);

    Vec3 directionToLocal(Vec3 world);

    boolean intersects(Level level, AABB local, AABB world);

    void push(BlockPos source, Function<SpaceFrame, @Nullable Thrust> contact, double liftBlocks, double maxSpeed);

    record Thrust(Vec3 point, Vec3 direction) {}
}
