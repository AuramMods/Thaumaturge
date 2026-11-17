package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.MenuThaumatorium;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundThaumatoriumTogglePayload(BlockPos pos, ResourceLocation recipeId)
        implements ModPayload {
    public static final ModPayload.Type<ServerboundThaumatoriumTogglePayload> TYPE =
            new ModPayload.Type<>(TTIds.rl("thaumatorium_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundThaumatoriumTogglePayload> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.BLOCK_POS,
                    ServerboundThaumatoriumTogglePayload::pos,
                    VanillaStreamCodecs.RESOURCE_LOCATION,
                    ServerboundThaumatoriumTogglePayload::recipeId,
                    ServerboundThaumatoriumTogglePayload::new);

    public static void handle(ServerboundThaumatoriumTogglePayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            BlockEntityThaumatorium machine = BlockMenuGuard.target(context, payload.pos(), MenuThaumatorium.class);
            if (machine != null
                    && context.player() instanceof ServerPlayer player
                    && player.level() instanceof ServerLevel level) {
                machine.toggleRecipe(level, player, payload.recipeId());
            }
        });
    }

    @Override
    public ModPayload.Type<ServerboundThaumatoriumTogglePayload> type() {
        return TYPE;
    }
}
