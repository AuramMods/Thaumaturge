package com.leclowndu93150.thaumaturge.mixin.world.level.chunk.storage;

import com.leclowndu93150.thaumaturge.state.ChunkAttachmentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
abstract class ChunkSerializerAttachmentsMixin {
    @Inject(method = "read", at = @At("RETURN"))
    private static void thaumaturge$loadAttachments(ServerLevel level, PoiManager poi, ChunkPos pos, CompoundTag input,
            CallbackInfoReturnable<ProtoChunk> cir) {
        ChunkAttachmentData.loadProto(cir.getReturnValue(), level.registryAccess(), input);
    }

    @Inject(method = "write", at = @At("RETURN"))
    private static void thaumaturge$saveAttachments(
            ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir) {
        ChunkAttachmentData.saveProto(chunk, cir.getReturnValue());
    }
}
