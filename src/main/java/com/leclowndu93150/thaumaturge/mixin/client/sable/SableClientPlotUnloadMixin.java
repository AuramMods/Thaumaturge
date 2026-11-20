package com.leclowndu93150.thaumaturge.mixin.client.sable;

import com.leclowndu93150.thaumaturge.content.golem.seals.ClientSealHolder;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import dev.ryanhcode.sable.sublevel.plot.ClientLevelPlot;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientLevelPlot.class, remap = false)
abstract class SableClientPlotUnloadMixin {
    @Inject(method = "onRemoveChunkHolder", at = @At("TAIL"))
    private void thaumaturge$forgetTrackedChunk(LevelChunk chunk, CallbackInfo ci) {
        ChunkPos pos = chunk.getPos();
        ClientSealHolder.forgetChunk(pos);
        ClientWardHolder.forgetChunk(pos);
    }
}
