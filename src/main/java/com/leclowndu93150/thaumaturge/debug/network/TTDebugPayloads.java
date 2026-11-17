package com.leclowndu93150.thaumaturge.debug.network;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class TTDebugPayloads {

    private TTDebugPayloads() {}

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        PayloadNetwork.Registrar registrar = PayloadNetwork.registrar();
        registrar.playToClient(
                ClientboundToggleRaycastDebugPayload.TYPE,
                ClientboundToggleRaycastDebugPayload.STREAM_CODEC,
                (payload, context) -> ToggleRaycastDebugHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundRaycastDebugPayload.TYPE,
                ClientboundRaycastDebugPayload.STREAM_CODEC,
                (payload, context) -> ToggleRaycastDebugHandler.handleServerRaycast(payload, context));
    }
}
