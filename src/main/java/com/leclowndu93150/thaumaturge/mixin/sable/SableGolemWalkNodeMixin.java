package com.leclowndu93150.thaumaturge.mixin.sable;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
abstract class SableGolemWalkNodeMixin extends NodeEvaluator {
    @Shadow
    protected abstract boolean canStartAt(BlockPos pos);

    @Shadow
    protected abstract Node getStartNode(BlockPos pos);

    @Inject(method = "getStart", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$startOnDeck(CallbackInfoReturnable<Node> cir) {
        if (!(mob instanceof EntityThaumaturgeGolem)
                || !(mob.getNavigation() instanceof GroundPathNavigation)
                || !mob.onGround() || mob.isInWater()) {
            return;
        }
        SubLevelAccess deck = SableCompanion.INSTANCE.getTrackingSubLevel(mob);
        if (deck == null) {
            return;
        }
        // A grounded golem is still standing in the previous deck pose until travel runs.
        Vec3 feet = deck.lastPose().transformPositionInverse(mob.position());
        BlockPos start = BlockPos.containing(feet.x, feet.y + 0.5, feet.z);
        if (canStartAt(start)) {
            cir.setReturnValue(getStartNode(start));
        }
    }
}
