package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public final class ServerboundAdvanceStageHandler {
    private ServerboundAdvanceStageHandler() {}

    public static void handle(ServerboundAdvanceStagePayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.advanceStage(player, payload.research());
            }
        });
    }
}
