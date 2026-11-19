package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class HoverRenderEvents {
    private HoverRenderEvents() {}

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            if (event.getPlayerSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new HoverRingLayer(renderer));
            }
        }
    }
}
