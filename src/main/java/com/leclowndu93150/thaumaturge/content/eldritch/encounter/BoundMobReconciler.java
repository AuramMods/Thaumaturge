package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class BoundMobReconciler {
    private BoundMobReconciler() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = event.getEntity();
        Optional<LabyrinthBinding> binding = LabyrinthBinding.on(entity);
        if (binding.isEmpty()) {
            return;
        }
        Optional<MazeRecord> record =
                LabyrinthService.byId(level.getServer(), binding.get().mazeId());
        if (record.isEmpty() || !record.get().state().tracks(entity.getUUID())) {
            event.setCanceled(true);
        }
    }
}
