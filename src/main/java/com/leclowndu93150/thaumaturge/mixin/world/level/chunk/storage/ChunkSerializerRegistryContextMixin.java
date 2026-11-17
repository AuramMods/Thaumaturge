package com.leclowndu93150.thaumaturge.mixin.world.level.chunk.storage;

import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
abstract class ChunkSerializerRegistryContextMixin {
    @Inject(method = "postLoadChunk", at = @At("RETURN"), cancellable = true)
    private static void thaumaturge$scopeChunkLoad(ServerLevel level, CompoundTag tag,
            CallbackInfoReturnable<LevelChunk.PostLoadProcessor> cir) {
        LevelChunk.PostLoadProcessor processor = cir.getReturnValue();
        if (processor != null) {
            cir.setReturnValue(chunk -> RegistryContext.withRegistries(level.registryAccess(), () -> {
                processor.run(chunk);
                return null;
            }));
        }
    }
}
