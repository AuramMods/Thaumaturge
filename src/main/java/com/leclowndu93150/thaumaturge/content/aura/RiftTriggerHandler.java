package com.leclowndu93150.thaumaturge.content.aura;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class RiftTriggerHandler {
    private static final int POLL_INTERVAL_TICKS = 20;

    private RiftTriggerHandler() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level) || level.getGameTime() % POLL_INTERVAL_TICKS != 0) {
            return;
        }
        BlockPos trigger = AuraManager.pollRiftTrigger(level);
        if (trigger != null
                && !ThaumaturgeCommonConfig.WUSS_MODE.get()
                && AuraTickHandler.canSpawnRift(level, trigger)) {
            EntityFluxRift.spawnNear(level, trigger);
        }
    }
}
