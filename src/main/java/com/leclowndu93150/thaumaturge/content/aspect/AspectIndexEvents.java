package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.network.ClientboundAspectIndexPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;
import com.leclowndu93150.thaumaturge.datamap.DataMapsUpdatedEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AspectIndexEvents {
    private static volatile boolean datamapsReady = false;
    private static volatile boolean buildRequested = false;
    private static volatile MinecraftServer runningServer = null;

    private AspectIndexEvents() {}

    @SubscribeEvent
    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        if (event.getRegistryKey() != Registries.ITEM) {
            return;
        }
        if (event.getCause() != DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD) {
            return;
        }
        datamapsReady = true;
        buildRequested = true;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        runningServer = event.getServer();
        buildRequested = true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        MinecraftServer server = runningServer;
        if (!buildRequested || !datamapsReady || server != event.getServer()) {
            return;
        }
        buildRequested = false;
        buildAndBroadcast(server);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        datamapsReady = false;
        buildRequested = false;
        runningServer = null;
        AspectIndexHolder.set(AspectIndex.EMPTY);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PayloadNetwork.sendToPlayer(player, new ClientboundAspectIndexPayload(AspectIndexHolder.getConcrete()));
        }
    }

    private static void buildAndBroadcast(MinecraftServer server) {
        AspectIndexFingerprint fingerprint = AspectIndexFingerprint.compute(server);
        AspectIndex index =
                AspectIndexFile.load(server.registryAccess(), fingerprint).orElse(null);
        if (index == null) {
            int itemCount = BuiltInRegistries.ITEM.size();
            int recipeCount = RecipeAccess.of(server.getRecipeManager()).getRecipes().size();
            index = AspectIndexBuilder.build(server.getRecipeManager(), server.registryAccess());
            AspectIndexFile.write(index, server.registryAccess(), fingerprint, itemCount, recipeCount);
        }
        AspectIndexHolder.set(index);
        if (!server.getPlayerList().getPlayers().isEmpty()) {
            PayloadNetwork.sendToAllPlayers(new ClientboundAspectIndexPayload(index));
        }
    }
}
