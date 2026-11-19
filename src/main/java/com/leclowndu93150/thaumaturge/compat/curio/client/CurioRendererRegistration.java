package com.leclowndu93150.thaumaturge.compat.curio.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CurioRendererRegistration {
    private CurioRendererRegistration() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(TTItems.THAUMOSTATIC_HARNESS.get(), HoverHarnessCurioRenderer::new);
            CuriosRendererRegistry.register(TTItems.GOGGLES_REVEALING.get(), GoggleCurioRenderer::new);
            CuriosRendererRegistry.register(TTItems.CURIOSITY_BAND.get(), CuriosityBandCurioRenderer::new);
        });
    }
}
