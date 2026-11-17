package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.state.AttachmentData;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.BaubleEvents;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundCloudJumpPayload() implements ModPayload {
    public static final Type<ServerboundCloudJumpPayload> TYPE = new Type<>(TTIds.rl("cloud_jump"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCloudJumpPayload> STREAM_CODEC =
            StreamCodec.unit(new ServerboundCloudJumpPayload());

    public static void handle(ServerboundCloudJumpPayload payload, PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (!ModList.get().isLoaded(TTIds.CURIOS)
                    || !ThaumaturgeCuriosCompat.isCurioEquipped(player, TTItems.CLOUD_RING.get())) {
                return;
            }
            if (player.onGround() || player.isInWater() || BaubleEvents.hasPendingCloudJump(player)) {
                return;
            }
            player.resetFallDistance();
            AttachmentData.set(player, TTAttachments.CLOUD_JUMP_TIME, player.level().getGameTime());
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
