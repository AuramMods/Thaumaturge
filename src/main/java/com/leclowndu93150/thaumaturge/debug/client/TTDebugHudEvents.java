package com.leclowndu93150.thaumaturge.debug.client;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(value = Dist.CLIENT, modid = TTIds.MODID)
public final class TTDebugHudEvents {
    private TTDebugHudEvents() {}

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TTIds.rl("raycast_debug"), new RaycastDebugOverlay());
    }
}
