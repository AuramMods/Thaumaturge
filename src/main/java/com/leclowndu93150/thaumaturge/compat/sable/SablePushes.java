package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import dev.ryanhcode.sable.api.event.SablePrePhysicsTickEvent;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.LevelEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.Nullable;

final class SablePushes implements SablePrePhysicsTickEvent {
    private static final double STANDARD_GRAVITY = 11.0;

    private final Map<ServerLevel, Map<UUID, Map<BlockPos, Push>>> pending = new ConcurrentHashMap<>();

    void queue(
            ServerSubLevel ship,
            BlockPos source,
            Function<SpaceFrame, SpaceFrame.@Nullable Thrust> contact,
            double liftBlocks,
            double maxSpeed) {
        ServerLevel level = ship.getLevel();
        pending.computeIfAbsent(level, world -> new ConcurrentHashMap<>())
                .computeIfAbsent(ship.getUniqueId(), id -> new ConcurrentHashMap<>())
                .put(source.immutable(), new Push(contact, liftBlocks, maxSpeed, level.getServer().getTickCount()));
    }

    void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            pending.remove(level);
        }
    }

    @Override
    public void prePhysicsTick(SubLevelPhysicsSystem system, double timeStep) {
        ServerLevel level = system.getLevel();
        Map<UUID, Map<BlockPos, Push>> levelPushes = pending.get(level);
        if (levelPushes == null || levelPushes.isEmpty()) {
            return;
        }
        // Sable steps physics before this level ticks its block entities.
        int requestTick = level.getServer().getTickCount() - 1;
        levelPushes.values().forEach(pushes -> pushes.values().removeIf(push -> push.tick() != requestTick));
        levelPushes.values().removeIf(Map::isEmpty);
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            return;
        }
        levelPushes.forEach((id, pushes) -> {
            SubLevel subLevel = container.getSubLevel(id);
            if (subLevel instanceof ServerSubLevel ship && !ship.isRemoved()) {
                RigidBodyHandle handle = system.getPhysicsHandle(ship);
                if (handle != null && handle.isValid()) {
                    apply(handle, ship, pushes.values(), timeStep);
                }
            }
        });
    }

    private void apply(RigidBodyHandle handle, ServerSubLevel ship, Collection<Push> pushes, double timeStep) {
        MassData mass = ship.getMassTracker();
        if (mass == null || mass.isInvalid()) {
            return;
        }
        Vector3dc velocity = handle.getLinearVelocity();
        Pose3dc pose = ship.logicalPose();
        SpaceFrame frame = new SubLevelFrame(ship, this);
        ForceTotal total = new ForceTotal();
        for (Push push : pushes) {
            SpaceFrame.Thrust thrust = push.contact().apply(frame);
            if (thrust == null) {
                continue;
            }
            Vec3 direction = thrust.direction().normalize();
            double along = velocity.x() * direction.x + velocity.y() * direction.y + velocity.z() * direction.z;
            if (along >= push.maxSpeed()) {
                continue;
            }
            double impulse = Math.min(
                    push.liftBlocks() * STANDARD_GRAVITY * timeStep, mass.getMass() * (push.maxSpeed() - along));
            Vec3 point = pose.transformPositionInverse(thrust.point());
            Vec3 localImpulse = pose.transformNormalInverse(direction).scale(impulse);
            total.applyImpulseAtPoint(
                    ship,
                    new Vector3d(point.x, point.y, point.z),
                    new Vector3d(localImpulse.x, localImpulse.y, localImpulse.z));
        }
        handle.applyForcesAndReset(total);
    }

    private record Push(
            Function<SpaceFrame, SpaceFrame.@Nullable Thrust> contact, double liftBlocks, double maxSpeed, int tick) {}
}
