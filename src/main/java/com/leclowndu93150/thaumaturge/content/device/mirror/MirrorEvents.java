package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.world.space.BlockForwarding;
import com.leclowndu93150.thaumaturge.content.world.space.BlocksMovedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID)
public final class MirrorEvents {
    private MirrorEvents() {}

    @SubscribeEvent
    public static void onBlocksMoved(BlocksMovedEvent event) {
        if (!event.withinLevel()) {
            return;
        }
        event.moves().forEach((from, to) -> {
            if (event.to().getBlockEntity(to) instanceof BlockEntityMirrorBase) {
                BlockForwarding.record(event.to(), from, to);
            }
        });
    }
}
