package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID)
public final class BoundMobReconciler {
    private BoundMobReconciler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = event.getEntity();
        // Deserialization can discard an entity before Forge posts its chunk-load event.
        if (entity.isRemoved()) {
            event.setCanceled(true);
            return;
        }
        if (!(entity instanceof Mob mob)) {
            return;
        }
        Optional<LabyrinthBinding> binding = LabyrinthBinding.on(mob);
        if (binding.isEmpty()) {
            return;
        }
        Optional<MazeRecord> record =
                LabyrinthService.byId(level.getServer(), binding.get().mazeId());
        if (record.isEmpty() || !record.get().state().tracks(mob.getUUID())) {
            event.setCanceled(true);
        }
    }
}
