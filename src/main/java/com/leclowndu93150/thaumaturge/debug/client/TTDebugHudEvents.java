package com.leclowndu93150.thaumaturge.debug.client;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT, modid = TTIds.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class TTDebugHudEvents {
    private TTDebugHudEvents() {}

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.ITEM_NAME.id(), "raycast_debug", new RaycastDebugOverlay());
    }
}
