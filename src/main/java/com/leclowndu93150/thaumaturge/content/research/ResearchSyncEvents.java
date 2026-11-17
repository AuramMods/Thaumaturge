package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ResearchSyncEvents {
    private ResearchSyncEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.getData(TTAttachments.KNOWLEDGE).takeSyncPending()) {
            player.syncData(TTAttachments.KNOWLEDGE);
        }
        AspectPools.flush(player);
    }
}
