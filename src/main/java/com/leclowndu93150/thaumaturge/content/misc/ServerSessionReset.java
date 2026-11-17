package com.leclowndu93150.thaumaturge.content.misc;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.content.effect.EssentiaSourceEffectTracker;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.research.CraftReferenceHolder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ServerSessionReset {
    private ServerSessionReset() {}

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DustTriggerSwapQueue.resetSession();
        CraftReferenceHolder.resetSession();
        EssentiaSourceEffectTracker.resetSession();
        AuraManager.resetSession();
    }
}
