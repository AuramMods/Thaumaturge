package com.leclowndu93150.thaumaturge.stage;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Receives the verified initiator of a placement or a deferred block transformation. */
public interface OwnedProcessing {
    void setOwner(UUID owner);

    static void onPlaced(Level level, BlockPos pos, @Nullable LivingEntity placer) {
        if (level instanceof ServerLevel server && placer instanceof Player player) {
            var verified = StageRules.verifiedActor(server, player);
            if (verified != null) setAt(server, pos, verified.getUUID());
        }
    }

    static void setAt(ServerLevel level, BlockPos pos, UUID owner) {
        if (level.getBlockEntity(pos) instanceof OwnedProcessing machine) {
            machine.setOwner(owner);
        }
    }
}
