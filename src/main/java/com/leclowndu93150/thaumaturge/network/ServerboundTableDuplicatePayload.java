package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundTableDuplicatePayload(BlockPos pos) implements ModPayload {
    public static final ModPayload.Type<ServerboundTableDuplicatePayload> TYPE =
            new ModPayload.Type<>(TTIds.rl("table_duplicate"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTableDuplicatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ServerboundTableDuplicatePayload::pos,
                    ServerboundTableDuplicatePayload::new);

    public static void handle(ServerboundTableDuplicatePayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntityResearchTable table = BlockMenuGuard.target(context, payload.pos(), MenuResearchTable.class);
            if (table != null) {
                table.duplicateNote((ServerPlayer) context.player());
            }
        });
    }

    @Override
    public ModPayload.Type<ServerboundTableDuplicatePayload> type() {
        return TYPE;
    }
}
