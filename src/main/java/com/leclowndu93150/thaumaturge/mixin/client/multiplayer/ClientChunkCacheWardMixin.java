package com.leclowndu93150.thaumaturge.mixin.client.multiplayer;

import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkCache.class)
abstract class ClientChunkCacheWardMixin {
    @Inject(method = "drop", at = @At("TAIL"))
    private void thaumaturge$forgetWards(int chunkX, int chunkZ, CallbackInfo callback) {
        // Forge's unload event only fires while the chunk is still in the cache's range.
        ClientWardHolder.forgetChunk(new ChunkPos(chunkX, chunkZ));
    }
}
