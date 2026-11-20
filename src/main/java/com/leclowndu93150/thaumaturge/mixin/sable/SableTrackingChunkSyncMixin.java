package com.leclowndu93150.thaumaturge.mixin.sable;

import com.leclowndu93150.thaumaturge.content.golem.GolemWorldEvents;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import dev.ryanhcode.sable.network.tcp.SableTCPPacket;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import dev.ryanhcode.sable.sublevel.system.SubLevelTrackingSystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SubLevelTrackingSystem.class, remap = false)
abstract class SableTrackingChunkSyncMixin {
    @Inject(method = "sendFullSync", at = @At("TAIL"))
    private void thaumaturge$syncChunkAttachments(ServerPlayer player, ServerSubLevel ship, SableTCPPacket extraPacket, CallbackInfo ci) {
        // Use separate packets after Sable's chunk bundle to preserve the vanilla bundle limit.
        for (PlotChunkHolder holder : ship.getPlot().getLoadedChunks()) {
            LevelChunk chunk = holder.getChunk();
            GolemWorldEvents.syncChunkSeals(player, chunk);
            WardHandler.syncChunk(player, chunk);
        }
    }
}
