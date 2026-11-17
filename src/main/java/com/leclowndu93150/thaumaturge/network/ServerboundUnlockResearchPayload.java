package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundUnlockResearchPayload(ResourceLocation research) implements ModPayload {
    public static final Type<ServerboundUnlockResearchPayload> TYPE = new Type<>(TTIds.rl("unlock_research"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundUnlockResearchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.RESOURCE_LOCATION,
                    ServerboundUnlockResearchPayload::research,
                    ServerboundUnlockResearchPayload::new);

    public static void handle(ServerboundUnlockResearchPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.unlockRequested(player, payload.research());
            }
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
