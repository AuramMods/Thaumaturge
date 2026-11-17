package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.content.aura.AuraData;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public final class ServerboundRequestAuraChunkHandler {
    private ServerboundRequestAuraChunkHandler() {}

    public static void handle(ServerboundRequestAuraChunkPayload payload, PayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        ChunkPos pos = new ChunkPos(payload.chunkX(), payload.chunkZ());
        AuraData data = AuraManager.chunkAt(level, pos);
        short base = data != null ? data.getBase() : 0;
        float vis = data != null ? data.getVis() : 0.0F;
        float flux = data != null ? data.getFlux() : 0.0F;
        PayloadNetwork.sendToPlayer(player, new ClientboundAuraSnapshotPayload(pos.x, pos.z, base, vis, flux));
    }
}
