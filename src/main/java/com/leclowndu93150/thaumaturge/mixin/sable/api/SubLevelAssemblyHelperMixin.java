package com.leclowndu93150.thaumaturge.mixin.sable.api;

import com.leclowndu93150.thaumaturge.compat.sable.SableAssembly;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.api.SubLevelAssemblyHelper", remap = false)
public class SubLevelAssemblyHelperMixin {
    @Inject(
            method = "moveBlocks(Lnet/minecraft/server/level/ServerLevel;"
                    + "Ldev/ryanhcode/sable/api/SubLevelAssemblyHelper$AssemblyTransform;Ljava/lang/Iterable;)V",
            at = @At("TAIL"))
    private static void thaumaturge$carryBlockData(
            ServerLevel level, AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci) {
        SableAssembly.blocksMoved(level, transform, blocks);
    }
}
