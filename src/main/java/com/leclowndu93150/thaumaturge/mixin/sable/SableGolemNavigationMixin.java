package com.leclowndu93150.thaumaturge.mixin.sable;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathNavigation.class)
abstract class SableGolemNavigationMixin {
    @Shadow @Final protected Mob mob;
    @Shadow @Final protected Level level;
    @Shadow protected Path path;
    @Shadow protected float maxDistanceToWaypoint;

    @Shadow
    protected abstract void doStuckDetection(Vec3 position);

    @Inject(method = "followThePath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/pathfinder/Path;getNextNodePos()Lnet/minecraft/core/BlockPos;"),
            cancellable = true)
    private void thaumaturge$advanceDeckWaypoint(CallbackInfo ci, @Local Vec3 navigationPosition) {
        SubLevelAccess deck = thaumaturge$waypointDeck();
        if (deck == null) {
            return;
        }
        // Travel applies the deck's movement after navigation runs.
        Vec3 local = deck.lastPose().transformPositionInverse(mob.position());
        Vec3 waypoint = path.getEntityPosAtNode(mob, path.getNextNodeIndex());
        if (Math.abs(local.x - waypoint.x) <= maxDistanceToWaypoint
                && Math.abs(local.z - waypoint.z) <= maxDistanceToWaypoint
                && Math.abs(local.y - waypoint.y) < 1.0) {
            path.advance();
        }
        doStuckDetection(navigationPosition);
        ci.cancel();
    }

    @Unique
    private @Nullable SubLevelAccess thaumaturge$waypointDeck() {
        if (!((Object) this instanceof GroundPathNavigation)
                || !(mob instanceof EntityThaumaturgeGolem) || path == null || path.isDone()) {
            return null;
        }
        SableCompanion sable = SableCompanion.INSTANCE;
        SubLevelAccess deck = sable.getTrackingSubLevel(mob);
        return deck != null && deck == sable.getContaining(level, path.getNextNode().asBlockPos()) ? deck : null;
    }
}
