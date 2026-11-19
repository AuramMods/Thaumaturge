package com.leclowndu93150.thaumaturge.state;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.jspecify.annotations.Nullable;

/** Bridges attachments from generation chunks to Forge's full-chunk capability. */
public final class ChunkAttachmentData {
    private static final String PROTO_TAG = "thaumaturge:attachments";

    private ChunkAttachmentData() {}

    public static <T> @Nullable T getExisting(ChunkAccess chunk, AttachmentKey<T> key) {
        Access proto = protoAccess(chunk);
        if (proto != null) {
            AttachmentStorage storage = proto.thaumaturge$getAttachments();
            return storage == null ? null : storage.getExisting(key);
        }
        return AttachmentData.getExistingDataOrNull(unwrap(chunk), key);
    }

    public static <T> void set(ChunkAccess chunk, RegistryAccess registries, AttachmentKey<T> key, T value) {
        Access proto = protoAccess(chunk);
        if (proto != null) {
            storage(proto, chunk, registries).set(key, value);
        } else {
            AttachmentData.set(unwrap(chunk), key, value);
        }
    }

    public static void saveProto(ChunkAccess chunk, CompoundTag output) {
        Access proto = protoAccess(chunk);
        AttachmentStorage storage = proto == null ? null : proto.thaumaturge$getAttachments();
        if (storage != null) {
            CompoundTag data = storage.save();
            if (!data.isEmpty()) {
                output.put(PROTO_TAG, data);
            }
        }
    }

    public static void loadProto(ProtoChunk chunk, RegistryAccess registries, CompoundTag input) {
        Access proto = protoAccess(chunk);
        if (proto != null && input.contains(PROTO_TAG, Tag.TAG_COMPOUND)) {
            storage(proto, chunk, registries).load(input.getCompound(PROTO_TAG));
        }
    }

    public static void promote(ProtoChunk source, LevelChunk target) {
        Access proto = protoAccess(source);
        AttachmentStorage storage = proto == null ? null : proto.thaumaturge$getAttachments();
        if (storage != null) {
            AttachmentData.storage(target).mergePersistent(storage.save());
        }
    }

    private static AttachmentStorage storage(Access proto, ChunkAccess chunk, RegistryAccess registries) {
        AttachmentStorage storage = proto.thaumaturge$getAttachments();
        if (storage == null) {
            storage = new AttachmentStorage(AttachmentData.registry(), () -> registries, () -> chunk.setUnsaved(true));
            proto.thaumaturge$setAttachments(storage);
        }
        return storage;
    }

    private static @Nullable Access protoAccess(ChunkAccess chunk) {
        return chunk instanceof ProtoChunk && !(chunk instanceof ImposterProtoChunk) ? (Access) chunk : null;
    }

    private static ChunkAccess unwrap(ChunkAccess chunk) {
        return chunk instanceof ImposterProtoChunk wrapper ? wrapper.getWrapped() : chunk;
    }

    public interface Access {
        @Nullable AttachmentStorage thaumaturge$getAttachments();

        void thaumaturge$setAttachments(AttachmentStorage storage);
    }
}
