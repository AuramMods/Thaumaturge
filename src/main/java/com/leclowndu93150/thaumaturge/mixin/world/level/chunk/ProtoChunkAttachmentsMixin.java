package com.leclowndu93150.thaumaturge.mixin.world.level.chunk;

import com.leclowndu93150.thaumaturge.state.AttachmentStorage;
import com.leclowndu93150.thaumaturge.state.ChunkAttachmentData;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ProtoChunk.class)
abstract class ProtoChunkAttachmentsMixin implements ChunkAttachmentData.Access {
    @Unique private @Nullable AttachmentStorage thaumaturge$attachments;

    @Override
    public @Nullable AttachmentStorage thaumaturge$getAttachments() {
        return thaumaturge$attachments;
    }

    @Override
    public void thaumaturge$setAttachments(AttachmentStorage storage) {
        thaumaturge$attachments = storage;
    }
}
