package com.leclowndu93150.thaumaturge.content.world.space;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public enum WorldFrame implements SpaceFrame {
    INSTANCE;

    @Override
    public boolean isWorld() {
        return true;
    }

    @Override
    public Vec3 toWorld(Vec3 local) {
        return local;
    }

    @Override
    public Vec3 toLocal(Vec3 world) {
        return world;
    }

    @Override
    public Vec3 directionToWorld(Vec3 local) {
        return local;
    }

    @Override
    public Vec3 directionToLocal(Vec3 world) {
        return world;
    }

    @Override
    public boolean intersects(Level level, AABB local, AABB world) {
        return local.intersects(world);
    }

    @Override
    public void push(
            BlockPos source, Function<SpaceFrame, @Nullable Thrust> contact, double liftBlocks, double maxSpeed) {}
}
