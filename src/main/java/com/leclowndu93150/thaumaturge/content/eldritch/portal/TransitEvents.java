package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TransitEvents {
    private TransitEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().dimension() != OuterLands.DIMENSION
                || !ThaumaturgeServerConfig.LABYRINTH.evacuateStrandedOnLogin.get()) {
            return;
        }
        ServerLevel outer = player.serverLevel();
        BlockPos feet = player.blockPosition();
        Optional<MazeRecord> record = LabyrinthService.find(outer, feet);
        if (record.isEmpty() || !record.get().plan().geometry().bounds().isInside(feet)) {
            LabyrinthTransit.returnToOrigin(player, record);
        } else {
            LabyrinthService.refresh(outer, outer.getChunkAt(feet), record.get());
            ArrivalPad.rescue(outer, record.get(), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            LabyrinthTransit.tick(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LabyrinthTransit.abandon(player);
        }
    }
}
