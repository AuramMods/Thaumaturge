package com.leclowndu93150.thaumaturge.content.recipe.dust;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.state.AttachmentData;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.stage.OwnedProcessing;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class DustTriggerTickHandler {
    private DustTriggerTickHandler() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        if (!AttachmentData.has(chunk, TTAttachments.DUST_TRIGGER_QUEUE)) {
            return;
        }
        DustTriggerSwapQueue queue = AttachmentData.get(chunk, TTAttachments.DUST_TRIGGER_QUEUE);
        if (queue.isEmpty()) {
            return;
        }
        DustTriggerSwapQueue.markChunkActive(level, chunk.getPos());
        for (PendingSwap entry : queue.entries()) {
            DustTriggerSwapQueue.markBlocked(level, entry.pos());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            tickLevel(level);
        }
    }

    private static void tickLevel(ServerLevel level) {
        Set<ChunkPos> active = DustTriggerSwapQueue.activeChunks(level);
        if (active.isEmpty()) {
            return;
        }
        for (ChunkPos cp : active) {
            if (!level.hasChunk(cp.x, cp.z)) {
                continue;
            }
            LevelChunk chunk = level.getChunk(cp.x, cp.z);
            DustTriggerSwapQueue queue = AttachmentData.get(chunk, TTAttachments.DUST_TRIGGER_QUEUE);
            if (queue.isEmpty()) {
                DustTriggerSwapQueue.markChunkClear(level, cp);
                continue;
            }
            tickChunk(level, chunk, queue);
        }
    }

    private static void tickChunk(ServerLevel level, LevelChunk chunk, DustTriggerSwapQueue queue) {
        List<PendingSwap> entries = queue.entries();
        List<PendingSwap> next = new ArrayList<>(entries.size());
        for (PendingSwap entry : entries) {
            if (!level.getBlockState(entry.pos()).is(entry.originalState().getBlock())) {
                DustTriggerSwapQueue.clearBlocked(level, entry.pos());
                continue;
            }
            if (entry.authority().isPresent() && !entry.authority().get().allows(level)) {
                next.add(entry);
                continue;
            }
            int remaining = entry.ticksRemaining() - 1;
            if (remaining <= 0) {
                complete(level, entry);
                continue;
            }
            next.add(entry.withTicks(remaining));
        }
        entries.clear();
        entries.addAll(next);
        chunk.setUnsaved(true);
        if (queue.isEmpty()) {
            DustTriggerSwapQueue.markChunkClear(level, chunk.getPos());
        }
    }

    private static void complete(ServerLevel level, PendingSwap entry) {
        BlockPos pos = entry.pos();
        BlockState resultState = entry.stateResult();
        ItemStack resultStack = entry.stackResult();
        if (resultState != null) {
            if (level.setBlock(pos, resultState, 3)) {
                entry.authority().flatMap(StageAuthorization::player)
                        .ifPresent(owner -> OwnedProcessing.setAt(level, pos, owner));
            }
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (resultStack != null && !resultStack.isEmpty()) {
                ItemStack copy = resultStack.copy();
                EntitySpecialItem drop =
                        new EntitySpecialItem(level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, copy);
                drop.setDeltaMovement(0.0, 0.0, 0.0);
                drop.setDefaultPickUpDelay();
                level.addFreshEntity(drop);
            }
        }
        Effects.bamf(level, pos).withSound().fancy().send();
        DustTriggerSwapQueue.clearBlocked(level, pos);
    }
}
