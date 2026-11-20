package com.leclowndu93150.thaumaturge.content.golem;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.world.space.BlocksMovedEvent;
import com.leclowndu93150.thaumaturge.event.combat.DamagePhaseEvent;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;

@EventBusSubscriber(modid = TTIds.MODID)
public final class GolemWorldEvents {
    private static final int TASK_CLEAR_INTERVAL_TICKS = 20;
    private static final int PLAYER_KILL_CREDIT_TICKS = 100;

    private GolemWorldEvents() {}

    @SubscribeEvent
    public static void creditOwnerForGolemDamage(DamagePhaseEvent.AfterDamage event) {
        if (event.getNewDamage() > 0.0F && event.getEntity().level() instanceof ServerLevel
                && event.getSource().getEntity() instanceof EntityThaumaturgeGolem golem
                && golem.grantsOwnerKillCredit() && golem.getOwner() instanceof Player owner) {
            // Loot and experience are awarded before the attack returns, even for a first-hit kill.
            var victim = event.getEntity();
            victim.setLastHurtByPlayer(owner);
            victim.lastHurtByPlayerTime = PLAYER_KILL_CREDIT_TICKS;
        }
    }

    @SubscribeEvent
    public static void onBlocksMoved(BlocksMovedEvent event) {
        SealHandler.carry(event.from(), event.to(), event.moves(), event.rotation());
        if (!event.withinLevel()) {
            return;
        }
        for (EntityThaumaturgeGolem golem : event.from()
                .getEntities(
                        TTEntities.THAUMATURGE_GOLEM.get(),
                        golem -> golem.hasRestriction() && event.moves().containsKey(golem.getRestrictCenter()))) {
            golem.restrictTo(event.moves().get(golem.getRestrictCenter()), (int) golem.getRestrictRadius());
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getChunk() instanceof LevelChunk chunk) {
            SealHandler.loadChunkSeals(serverLevel, chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getChunk() instanceof LevelChunk chunk) {
            SealHandler.unloadChunkSeals(serverLevel, chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        syncChunkSeals(event.getPlayer(), event.getChunk());
    }

    public static void syncChunkSeals(ServerPlayer player, LevelChunk chunk) {
        for (SealEntity seal : SealHandler.getSealsInChunk(chunk)) {
            PayloadNetwork.sendToPlayer(player, ClientboundSealPayload.update(seal));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.getGameTime() % TASK_CLEAR_INTERVAL_TICKS == 0) {
            TaskBoard.of(serverLevel).sweep(serverLevel);
        }
        SealHandler.tickSealEntities(serverLevel);
    }
}
