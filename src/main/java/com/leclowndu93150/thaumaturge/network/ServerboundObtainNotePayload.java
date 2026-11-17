package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundObtainNotePayload(ResourceLocation entry, int ordinal) implements ModPayload {
    public static final ModPayload.Type<ServerboundObtainNotePayload> TYPE =
            new ModPayload.Type<>(TTIds.rl("obtain_note"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundObtainNotePayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.RESOURCE_LOCATION,
                    ServerboundObtainNotePayload::entry,
                    ByteBufCodecs.VAR_INT,
                    ServerboundObtainNotePayload::ordinal,
                    ServerboundObtainNotePayload::new);

    public static void handle(ServerboundObtainNotePayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchNotes.obtainNote(player, payload.entry(), payload.ordinal());
            }
        });
    }

    @Override
    public ModPayload.Type<ServerboundObtainNotePayload> type() {
        return TYPE;
    }
}
