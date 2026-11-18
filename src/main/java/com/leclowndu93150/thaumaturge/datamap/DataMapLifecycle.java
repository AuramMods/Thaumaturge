package com.leclowndu93150.thaumaturge.datamap;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID)
public final class DataMapLifecycle {
    private DataMapLifecycle() {}
    @SubscribeEvent public static void onServerStopped(ServerStoppedEvent event) {
        DataMaps.clear(event.getServer().registryAccess());
    }
}
