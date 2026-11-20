package com.leclowndu93150.thaumaturge.mixin.client.sable;

import com.leclowndu93150.thaumaturge.content.golem.seals.ClientSealHolder;
import dev.ryanhcode.sable.sublevel.plot.ClientLevelPlot;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientLevelPlot.class, remap = false)
abstract class SableClientPlotSealMixin {
    @Inject(method = "onRemoveChunkHolder", at = @At("TAIL"))
    private void thaumaturge$forgetSeals(LevelChunk chunk, CallbackInfo ci) {
        ClientSealHolder.forgetChunk(chunk.getPos());
    }
}
