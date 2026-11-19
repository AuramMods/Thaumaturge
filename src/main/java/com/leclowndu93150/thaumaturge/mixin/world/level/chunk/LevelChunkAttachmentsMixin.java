package com.leclowndu93150.thaumaturge.mixin.world.level.chunk;

import com.leclowndu93150.thaumaturge.state.ChunkAttachmentData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
abstract class LevelChunkAttachmentsMixin {
    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V",
            at = @At("RETURN"))
    private void thaumaturge$promoteAttachments(
            ServerLevel level, ProtoChunk source, LevelChunk.PostLoadProcessor postLoad, CallbackInfo ci) {
        ChunkAttachmentData.promote(source, (LevelChunk) (Object) this);
    }
}
