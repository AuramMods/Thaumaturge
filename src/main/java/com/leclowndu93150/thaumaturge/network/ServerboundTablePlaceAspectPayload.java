package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundTablePlaceAspectPayload(BlockPos pos, int q, int r, Optional<ResourceLocation> aspect)
        implements ModPayload {
    public static final ModPayload.Type<ServerboundTablePlaceAspectPayload> TYPE =
            new ModPayload.Type<>(TTIds.rl("table_place_aspect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTablePlaceAspectPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ServerboundTablePlaceAspectPayload::pos,
                    ByteBufCodecs.VAR_INT,
                    ServerboundTablePlaceAspectPayload::q,
                    ByteBufCodecs.VAR_INT,
                    ServerboundTablePlaceAspectPayload::r,
                    ByteBufCodecs.optional(VanillaStreamCodecs.RESOURCE_LOCATION),
                    ServerboundTablePlaceAspectPayload::aspect,
                    ServerboundTablePlaceAspectPayload::new);

    public static void handle(ServerboundTablePlaceAspectPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntityResearchTable table = BlockMenuGuard.target(context, payload.pos(), MenuResearchTable.class);
            if (table == null) {
                return;
            }
            ServerPlayer player = (ServerPlayer) context.player();
            Holder<IAspect> holder = payload.aspect()
                    .flatMap(id -> player.registryAccess()
                            .lookupOrThrow(IAspect.REGISTRY_KEY)
                            .get(ResourceKey.create(IAspect.REGISTRY_KEY, id)))
                    .map(reference -> (Holder<IAspect>) reference)
                    .orElse(null);
            table.placeAspect(player, new HexGrid.Hex(payload.q(), payload.r()), holder);
        });
    }

    @Override
    public ModPayload.Type<ServerboundTablePlaceAspectPayload> type() {
        return TYPE;
    }
}
