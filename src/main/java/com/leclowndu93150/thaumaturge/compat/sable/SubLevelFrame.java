package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import dev.ryanhcode.sable.api.math.LevelReusedVectors;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.LevelExtension;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

final class SubLevelFrame implements SpaceFrame {
    private final SubLevelAccess subLevel;
    private final SablePushes pushes;

    SubLevelFrame(SubLevelAccess subLevel, SablePushes pushes) {
        this.subLevel = subLevel;
        this.pushes = pushes;
    }

    @Override
    public boolean isWorld() {
        return false;
    }

    @Override
    public Vec3 toWorld(Vec3 local) {
        return subLevel.logicalPose().transformPosition(local);
    }

    @Override
    public Vec3 toLocal(Vec3 world) {
        return subLevel.logicalPose().transformPositionInverse(world);
    }

    @Override
    public Vec3 directionToWorld(Vec3 local) {
        return subLevel.logicalPose().transformNormal(local);
    }

    @Override
    public Vec3 directionToLocal(Vec3 world) {
        return subLevel.logicalPose().transformNormalInverse(world);
    }

    @Override
    public boolean intersects(Level level, AABB local, AABB world) {
        Pose3dc pose = subLevel.logicalPose();
        LevelReusedVectors vectors = ((LevelExtension) level).sable$getJOMLSink();
        Vec3 localCenter = pose.transformPosition(local.getCenter());
        Vector3d localSize = new Vector3d(local.getXsize(), local.getYsize(), local.getZsize())
                .mul(pose.scale())
                .absolute();
        OrientedBoundingBox3d rotated = new OrientedBoundingBox3d(
                new Vector3d(localCenter.x, localCenter.y, localCenter.z), localSize, pose.orientation(), vectors);
        Vec3 worldCenter = world.getCenter();
        OrientedBoundingBox3d axisAligned = new OrientedBoundingBox3d(
                worldCenter.x, worldCenter.y, worldCenter.z,
                world.getXsize(), world.getYsize(), world.getZsize(), new Quaterniond(), vectors);
        double penetration = OrientedBoundingBox3d.sat(rotated, axisAligned).lengthSquared();
        return penetration > 0.0 && Double.isFinite(penetration);
    }

    @Override
    public void push(
            BlockPos source, Function<SpaceFrame, @Nullable Thrust> contact, double liftBlocks, double maxSpeed) {
        if (subLevel instanceof ServerSubLevel ship) {
            pushes.queue(ship, source, contact, liftBlocks, maxSpeed);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SubLevelFrame frame
                && frame.subLevel.getUniqueId().equals(subLevel.getUniqueId());
    }

    @Override
    public int hashCode() {
        return subLevel.getUniqueId().hashCode();
    }
}
