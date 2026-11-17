package com.leclowndu93150.thaumaturge.debug;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.debug.network.ClientboundRaycastDebugPayload;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;

@EventBusSubscriber(modid = TTIds.MODID)
public class TTDebugEvents {
    private static final Set<UUID> RAYCAST_DEBUG_PLAYERS = ConcurrentHashMap.newKeySet();

    public static void toggleRaycastDebug(ServerPlayer player) {
        if (!RAYCAST_DEBUG_PLAYERS.remove(player.getUUID())) {
            RAYCAST_DEBUG_PLAYERS.add(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        RAYCAST_DEBUG_PLAYERS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        Player player = event.player;
        if (!RAYCAST_DEBUG_PLAYERS.contains(player.getUUID())
                || !LoadedChunks.contains(player.level(), player.blockPosition())) return;
        HitResult hitResult = ScanRaycastHelper.performRaycast(player);
        PayloadNetwork.sendToPlayer((ServerPlayer) player, new ClientboundRaycastDebugPayload(hitResult));
    }
}
