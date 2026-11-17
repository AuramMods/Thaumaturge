package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.state.AttachmentData;

import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

final class TaintLegacyData {
    private TaintLegacyData() {}

    static void importChunk(ServerLevel level, LevelChunk chunk) {
        if (AttachmentData.get(chunk, TTAttachments.TAINT_LEGACY_IMPORTED)) return;
        TaintEcologyState.get(level).copyTo(chunk);
        TaintBiomeState.get(level).copyTo(chunk);
        AttachmentData.set(chunk, TTAttachments.TAINT_LEGACY_IMPORTED, true);
        chunk.setUnsaved(true);
    }
}
