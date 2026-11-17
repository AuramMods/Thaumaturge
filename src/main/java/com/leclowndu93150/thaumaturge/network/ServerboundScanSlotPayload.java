package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.state.AttachmentData;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.network.transport.ModPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public record ServerboundScanSlotPayload(int slot) implements ModPayload {
    public static final int SELF = -1;
    public static final int CHANNEL_TICKS = 25;
    private static final int LATENCY_ALLOWANCE_TICKS = 5;

    public static final Type<ServerboundScanSlotPayload> TYPE = new Type<>(TTIds.rl("scan_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundScanSlotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerboundScanSlotPayload::slot, ServerboundScanSlotPayload::new);

    public static void handle(ServerboundScanSlotPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            AbstractContainerMenu menu = player.containerMenu;
            long now = player.level().getGameTime();
            if (!(menu.getCarried().getItem() instanceof ThaumometerItem)
                    || now - AttachmentData.get(player, TTAttachments.SLOT_SCAN_TIME) < CHANNEL_TICKS - LATENCY_ALLOWANCE_TICKS) {
                return;
            }
            AttachmentData.set(player, TTAttachments.SLOT_SCAN_TIME, now);
            if (payload.slot() == SELF) {
                ScanningManager.scan(player, ScanTarget.entity(player));
                return;
            }
            if (payload.slot() < 0 || payload.slot() >= menu.slots.size()) {
                return;
            }
            Slot slot = menu.getSlot(payload.slot());
            if (slot.hasItem() && slot.mayPickup(player) && !(slot instanceof ResultSlot)) {
                ScanningManager.scan(player, ScanTarget.stack(slot.getItem()));
            }
        });
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
