package com.leclowndu93150.thaumaturge.state;

import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record AttachmentSyncPayload(int entityId, ResourceLocation key, Optional<byte[]> data) implements ModPayload {
    public static final Type<AttachmentSyncPayload> TYPE =
            new Type<>(new ResourceLocation("thaumaturge", "attachment_sync"));
    public static final StreamCodec<ByteBuf, AttachmentSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AttachmentSyncPayload::entityId,
            VanillaStreamCodecs.RESOURCE_LOCATION, AttachmentSyncPayload::key,
            ByteBufCodecs.optional(ByteBufCodecs.BYTE_ARRAY), AttachmentSyncPayload::data, AttachmentSyncPayload::new);

    @Override
    public Type<AttachmentSyncPayload> type() { return TYPE; }
}
