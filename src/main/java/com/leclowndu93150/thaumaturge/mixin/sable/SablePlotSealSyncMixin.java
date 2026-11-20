package com.leclowndu93150.thaumaturge.mixin.sable;

import com.leclowndu93150.thaumaturge.content.golem.GolemWorldEvents;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerLevelPlot.class, remap = false)
abstract class SablePlotSealSyncMixin {
    @Inject(method = {"addChunkHolder", "load"}, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Ldev/ryanhcode/sable/sublevel/plot/SubLevelPlayerChunkSender;sendChunk("
                    + "Ljava/util/function/Consumer;Lnet/minecraft/world/level/lighting/LevelLightEngine;"
                    + "Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void thaumaturge$syncChunkSeals(CallbackInfo ci, @Local ServerPlayer player, @Local LevelChunk chunk) {
        GolemWorldEvents.syncChunkSeals(player, chunk);
    }
}
