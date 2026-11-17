package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.level.BlockEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class DustTriggerBlockedBreakHandler {
    private DustTriggerBlockedBreakHandler() {}

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (DustTriggerSwapQueue.isBlocked(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
        }
    }
}
