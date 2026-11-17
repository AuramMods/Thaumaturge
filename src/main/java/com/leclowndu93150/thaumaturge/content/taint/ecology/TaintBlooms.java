package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.state.AttachmentData;

import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class TaintBlooms {
    public static final int PROTECTION_RADIUS = 8;
    private static final double PROTECTION_RADIUS_SQ = PROTECTION_RADIUS * PROTECTION_RADIUS;

    private TaintBlooms() {}

    public static void register(ServerLevel level, BlockPos pos) {
        AttachmentData.get(level, TTAttachments.TAINT_BLOOMS).add(pos);
    }

    public static void unregister(ServerLevel level, BlockPos pos) {
        TaintBloomIndex index = AttachmentData.getExistingDataOrNull(level, TTAttachments.TAINT_BLOOMS);
        if (index == null) {
            return;
        }
        index.remove(pos);
        if (index.isEmpty()) {
            AttachmentData.remove(level, TTAttachments.TAINT_BLOOMS);
        }
    }

    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        TaintBloomIndex index = AttachmentData.getExistingDataOrNull(level, TTAttachments.TAINT_BLOOMS);
        return index != null && index.covers(level, pos, PROTECTION_RADIUS_SQ);
    }
}
