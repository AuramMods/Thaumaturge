package com.leclowndu93150.thaumaturge.datamap;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID)
public final class DataMapLifecycle {
    private DataMapLifecycle() {}
    @SubscribeEvent
    public static void syncDataPacks(OnDatapackSyncEvent event) {
        var remote = event.getPlayers().stream()
                .map(player -> player.connection.connection)
                .filter(connection -> !connection.isMemoryConnection())
                .toList();
        if (remote.isEmpty()) return;
        var fragments = DataMapSync.snapshot(event.getPlayerList().getServer().registryAccess(), DataMaps.CATALOG);
        for (var connection : remote) {
            for (var fragment : fragments) PayloadNetwork.sendDataMapFragment(connection, fragment);
        }
    }

    @SubscribeEvent public static void onServerStopped(ServerStoppedEvent event) {
        DataMaps.clear(event.getServer().registryAccess());
    }
}
