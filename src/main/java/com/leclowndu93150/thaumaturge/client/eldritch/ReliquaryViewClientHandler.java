package com.leclowndu93150.thaumaturge.client.eldritch;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.network.ClientboundReliquaryViewPayload;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ReliquaryViewClientHandler {
    private ReliquaryViewClientHandler() {}

    public static void handle(ClientboundReliquaryViewPayload payload, PayloadContext context) {
        context.enqueueWork(() -> ReliquaryViewHolder.put(payload.pos(), payload.view()));
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ReliquaryViewHolder.clear();
    }

    @SubscribeEvent
    public static void onRespawn(ClientPlayerNetworkEvent.Clone event) {
        ReliquaryViewHolder.clear();
    }
}
