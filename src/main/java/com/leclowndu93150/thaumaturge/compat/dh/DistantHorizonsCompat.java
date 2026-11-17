package com.leclowndu93150.thaumaturge.compat.dh;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class DistantHorizonsCompat {
    private DistantHorizonsCompat() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded(TTIds.DISTANT_HORIZONS)) {
            event.enqueueWork(WarpMistFogEvent::register);
        }
    }
}
