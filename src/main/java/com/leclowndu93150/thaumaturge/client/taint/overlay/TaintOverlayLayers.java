package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class TaintOverlayLayers {
    private TaintOverlayLayers() {}

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?> living) {
                addTaintedLayer(living);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addTaintedLayer(LivingEntityRenderer<?, ?> renderer) {
        renderer.addLayer(new TaintOverlayLayer(renderer));
    }
}
