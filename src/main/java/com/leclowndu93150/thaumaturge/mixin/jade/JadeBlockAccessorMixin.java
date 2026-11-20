package com.leclowndu93150.thaumaturge.mixin.jade;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "snownee.jade.impl.BlockAccessorImpl", remap = false)
abstract class JadeBlockAccessorMixin {
    @WrapOperation(method = "lambda$handleRequest$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;distSqr(Lnet/minecraft/core/Vec3i;)D", remap = true))
    private static double thaumaturge$measurePhysicalReach(BlockPos target, Vec3i observer, Operation<Double> original,
            @Local(argsOnly = true) ServerPlayer player) {
        var level = player.serverLevel();
        var frame = LevelSpaces.frameOf(level, target);
        if (frame.isWorld()) {
            return original.call(target, observer);
        }
        return frame.toWorld(Vec3.atCenterOf(target))
                .distanceToSqr(LevelSpaces.toWorld(level, Vec3.atCenterOf(observer)));
    }
}
