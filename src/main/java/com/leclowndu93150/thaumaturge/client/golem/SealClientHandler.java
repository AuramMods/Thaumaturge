package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.seals.ClientSealHolder;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class SealClientHandler {
    private SealClientHandler() {}

    public static void handle(ClientboundSealPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.seal().isPresent()) {
                ClientSealHolder.put(payload.seal().get());
            } else {
                ClientSealHolder.remove(payload.pos());
            }
        });
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ClientSealHolder.clear();
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSealHolder.clear();
    }
}
