package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.research.decon.MenuDeconstructionTable;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundDeconCollectPayload(BlockPos pos) implements ModPayload {
    public static final ModPayload.Type<ServerboundDeconCollectPayload> TYPE =
            new ModPayload.Type<>(TTIds.rl("decon_collect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDeconCollectPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS, ServerboundDeconCollectPayload::pos, ServerboundDeconCollectPayload::new);

    public static void handle(ServerboundDeconCollectPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntityDeconstructionTable table =
                    BlockMenuGuard.target(context, payload.pos(), MenuDeconstructionTable.class);
            if (table != null) {
                table.collect((ServerPlayer) context.player());
            }
        });
    }

    @Override
    public ModPayload.Type<ServerboundDeconCollectPayload> type() {
        return TYPE;
    }
}
