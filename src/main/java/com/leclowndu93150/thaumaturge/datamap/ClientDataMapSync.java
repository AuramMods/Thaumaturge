package com.leclowndu93150.thaumaturge.datamap;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.slf4j.Logger;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ClientDataMapSync {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DataMapSync.Receiver RECEIVER = new DataMapSync.Receiver(DataMaps.CATALOG);

    private ClientDataMapSync() {}

    public static void receive(Connection connection, DataMapSync.Fragment fragment) {
        var listener = Minecraft.getInstance().getConnection();
        if (listener == null || listener.getConnection() != connection) return;
        try {
            RECEIVER.receive(connection, fragment);
        } catch (RuntimeException failure) {
            disconnect(connection, failure);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void tagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED || !event.shouldUpdateStaticData()) return;
        var listener = Minecraft.getInstance().getConnection();
        if (listener == null) return;
        var connection = listener.getConnection();
        try {
            RECEIVER.tagsApplied(connection, event.getRegistryAccess());
        } catch (RuntimeException failure) {
            disconnect(connection, failure);
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        // A null connection means Forge is clearing a screen/world without a player.
        RECEIVER.clear(event.getConnection());
    }

    private static void disconnect(Connection connection, RuntimeException failure) {
        LOGGER.error("Cannot synchronize Thaumaturge data maps", failure);
        RECEIVER.clear(connection);
        connection.disconnect(Component.literal("Cannot synchronize Thaumaturge data maps: " + failure.getMessage()));
    }
}
