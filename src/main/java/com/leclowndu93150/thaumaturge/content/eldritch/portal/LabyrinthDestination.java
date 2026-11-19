package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

record LabyrinthDestination(ServerLevel level, Vec3 pos, float yaw, float pitch, boolean missingRespawnBlock) {
    LabyrinthDestination(ServerLevel level, Vec3 pos, float yaw, float pitch) {
        this(level, pos, yaw, pitch, false);
    }

    boolean teleport(ServerPlayer player, boolean playPortalSound) {
        if (player.isRemoved()) {
            return false;
        }
        if (missingRespawnBlock) {
            player.connection.send(new ClientboundGameEventPacket(
                    ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        }
        if (player.serverLevel() == level) {
            player.connection.teleport(pos.x, pos.y, pos.z, yaw, pitch);
            player.connection.resetPosition();
            if (playPortalSound) {
                player.connection.send(new ClientboundLevelEventPacket(
                        LevelEvent.SOUND_PORTAL_TRAVEL, BlockPos.ZERO, 0, false));
            }
            return true;
        }
        return player.changeDimension(level, new ITeleporter() {
            @Override
            public PortalInfo getPortalInfo(Entity entity, ServerLevel destination,
                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                return new PortalInfo(pos, Vec3.ZERO, yaw, pitch);
            }

            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentLevel, ServerLevel destination,
                    float yaw, Function<Boolean, Entity> repositionEntity) {
                // Keep the explicit destination without generating a vanilla exit portal or End platform.
                return repositionEntity.apply(false);
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel source, ServerLevel destination) {
                return playPortalSound;
            }
        }) == player;
    }
}
