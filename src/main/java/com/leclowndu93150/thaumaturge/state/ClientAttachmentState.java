package com.leclowndu93150.thaumaturge.state;

import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class ClientAttachmentState {
    private ClientAttachmentState() {}

    static void apply(AttachmentSyncPayload payload) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var owner = level.getEntity(payload.entityId());
        if (owner == null || owner.isRemoved()) return; // A queued update can outlive the entity's capability.
        AttachmentKey<?> key = AttachmentData.registry().get(payload.key());
        if (key == null || key.syncCodec() == null) {
            throw new DecoderException("Unknown synchronized state " + payload.key());
        }
        AttachmentStorage storage = AttachmentData.storage(owner);
        if (payload.data().isEmpty()) {
            storage.remove(key);
            return;
        }
        var buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.data().get()), level.registryAccess());
        try {
            decode(storage, key, buffer);
            if (buffer.isReadable()) throw new DecoderException("Trailing bytes in state " + payload.key());
        } finally {
            buffer.release();
        }
    }

    private static <T> void decode(AttachmentStorage storage, AttachmentKey<T> key, RegistryFriendlyByteBuf buffer) {
        storage.set(key, key.syncCodec().decode(buffer));
    }
}
