package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.aura.ClientAuraCache;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.manager.FXManagerRegistry;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayCache;
import com.leclowndu93150.thaumaturge.client.screen.research.ThaumonomiconBrowserScreen;
import com.leclowndu93150.thaumaturge.client.taint.TaintEnvironmentClientState;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndex;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ClientSessionReset {
    private ClientSessionReset() {}

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            clearLevelState();
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clearLevelState();
        RecipeDisplayCache.clear();
        ThaumonomiconBrowserScreen.resetNavigation();
        var connection = event.getConnection();
        if (connection != null && !connection.isMemoryConnection()) {
            // An integrated server owns its shared index until it stops.
            AspectIndexHolder.set(AspectIndex.EMPTY);
        }
    }

    private static void clearLevelState() {
        FXManagerRegistry.clearAll();
        LateWorldRenderQueue.clear();
        ClientAuraCache.clear();
        TaintEnvironmentClientState.reset();
    }
}
