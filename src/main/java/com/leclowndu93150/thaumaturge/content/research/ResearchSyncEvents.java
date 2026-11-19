package com.leclowndu93150.thaumaturge.content.research;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.state.AttachmentData;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ResearchSyncEvents {
    private ResearchSyncEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // Forge can finish a player tick after removal invalidates the capability.
        if (!(event.player instanceof ServerPlayer player) || player.isRemoved()) {
            return;
        }
        if (AttachmentData.get(player, TTAttachments.KNOWLEDGE).takeSyncPending()) {
            AttachmentData.sync(player, TTAttachments.KNOWLEDGE);
        }
        AspectPools.flush(player);
    }
}
