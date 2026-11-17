package com.leclowndu93150.thaumaturge.client.warding;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.particle.WardFlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import com.leclowndu93150.thaumaturge.network.ClientboundWardChunkPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundWardUpdatePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.ChunkEvent;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class WardClientHandler {
    private static final float CENTER = 0.5F;

    private WardClientHandler() {}

    public static void spawnHitEffect(ClientLevel level, BlockPos pos, Direction face, @Nullable HitResult hit) {
        level.addParticle(
                WardFlashParticleOptions.at(pos, face, hit == null ? null : hit.getLocation()),
                pos.getX() + CENTER,
                pos.getY() + CENTER,
                pos.getZ() + CENTER,
                0.0,
                0.0,
                0.0);
    }

    public static void handleChunk(ClientboundWardChunkPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            UUID self = context.player().getUUID();
            List<BlockPos> owned = new ArrayList<>();
            List<BlockPos> foreign = new ArrayList<>();
            for (ClientboundWardChunkPayload.Group group : payload.groups()) {
                List<BlockPos> target = self.equals(group.owner()) ? owned : foreign;
                for (int packed : group.positions()) {
                    target.add(payload.unpack(packed));
                }
            }
            ClientWardHolder.replaceChunk(payload.chunk(), owned, foreign);
        });
    }

    public static void handleUpdate(ClientboundWardUpdatePayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.owner().isPresent()) {
                ClientWardHolder.put(
                        payload.pos(),
                        payload.owner().get().equals(context.player().getUUID()));
            } else {
                ClientWardHolder.remove(payload.pos());
            }
        });
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ClientWardHolder.forgetChunk(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientWardHolder.clear();
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientWardHolder.clear();
    }
}
