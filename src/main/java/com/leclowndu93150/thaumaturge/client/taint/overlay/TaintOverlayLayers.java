package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class TaintOverlayLayers {
    private TaintOverlayLayers() {}

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            EntityRenderer<?> renderer = event.getEntityRenderer(type);
            if (renderer instanceof LivingEntityRenderer<?, ?> living) {
                addTaintedLayer(living);
            }
        }
    }

    private static <T extends LivingEntity, M extends EntityModel<T>> void addTaintedLayer(
            LivingEntityRenderer<T, M> renderer) {
        renderer.addLayer(new TaintOverlayLayer<>(renderer));
    }
}
