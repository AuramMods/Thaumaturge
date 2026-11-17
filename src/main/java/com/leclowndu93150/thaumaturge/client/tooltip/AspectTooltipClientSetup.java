package com.leclowndu93150.thaumaturge.client.tooltip;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectChipsTooltip;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class AspectTooltipClientSetup {
    private AspectTooltipClientSetup() {}

    @SubscribeEvent
    public static void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AspectChipsTooltip.class, AspectChipsClientTooltip::new);
    }
}
