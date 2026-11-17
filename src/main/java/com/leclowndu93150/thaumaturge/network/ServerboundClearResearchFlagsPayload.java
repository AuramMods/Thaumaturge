package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import java.util.List;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundClearResearchFlagsPayload(ResourceLocation research, List<ResearchFlag> flags)
        implements ModPayload {
    public static final Type<ServerboundClearResearchFlagsPayload> TYPE = new Type<>(TTIds.rl("clear_research_flags"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundClearResearchFlagsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.RESOURCE_LOCATION,
                    ServerboundClearResearchFlagsPayload::research,
                    ResearchFlag.STREAM_CODEC.apply(ByteBufCodecs.list(ResearchFlag.values().length)),
                    ServerboundClearResearchFlagsPayload::flags,
                    ServerboundClearResearchFlagsPayload::new);

    public static void handle(ServerboundClearResearchFlagsPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            var knowledge = PlayerResearchAccess.of(player);
            if (!knowledge.knows(payload.research())) return;
            boolean changed = false;
            for (ResearchFlag flag : payload.flags()) {
                if (knowledge.removeFlag(payload.research(), flag)) {
                    changed = true;
                }
            }
            if (changed) knowledge.queueSync(player);
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
