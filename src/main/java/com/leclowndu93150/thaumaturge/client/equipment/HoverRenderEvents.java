package com.leclowndu93150.thaumaturge.client.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class HoverRenderEvents {
    private HoverRenderEvents() {}

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new HoverRingLayer(renderer));
        }
    }
}
