package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.mixin.server.level.PlayerRespawnLogicInvoker;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.Vec3;

final class ReturnPointFinder {
    private static final int RING_RADIUS = 4;
    private static final int RING_HEIGHT = 3;

    private ReturnPointFinder() {}

    static LabyrinthDestination find(ServerPlayer player, ServerLevel level, BlockPos anchor, int surfaceRadius) {
        Optional<BlockPos> spot = SafeSpot.near(level, anchor, RING_RADIUS, RING_HEIGHT)
                .or(() -> surface(level, anchor, surfaceRadius));
        if (spot.isPresent()) {
            return new LabyrinthDestination(
                    level, Vec3.atBottomCenterOf(spot.get()), player.getYRot(), player.getXRot());
        }
        return respawn(player);
    }

    private static LabyrinthDestination respawn(ServerPlayer player) {
        var server = player.serverLevel().getServer();
        BlockPos respawnPos = player.getRespawnPosition();
        ServerLevel respawnLevel = server.getLevel(player.getRespawnDimension());
        boolean hasRespawnBlock = respawnPos != null && respawnLevel != null;
        if (hasRespawnBlock) {
            float yaw = player.getRespawnAngle();
            Optional<Vec3> position = Player.findRespawnPositionAndUseSpawnBlock(
                    respawnLevel, respawnPos, yaw, player.isRespawnForced(), true);
            if (position.isPresent()) {
                Vec3 landing = position.get();
                var block = respawnLevel.getBlockState(respawnPos).getBlock();
                if (block instanceof BedBlock || block instanceof RespawnAnchorBlock) {
                    Vec3 facing = Vec3.atBottomCenterOf(respawnPos).subtract(landing).normalize();
                    yaw = Mth.wrapDegrees((float) Mth.atan2(facing.z, facing.x) * Mth.RAD_TO_DEG - 90.0F);
                }
                return new LabyrinthDestination(respawnLevel, landing, yaw, 0.0F);
            }
        }
        ServerLevel overworld = server.overworld();
        return new LabyrinthDestination(overworld, Vec3.atBottomCenterOf(sharedSpawn(overworld, player)),
                0.0F, 0.0F, hasRespawnBlock);
    }

    private static BlockPos sharedSpawn(ServerLevel level, ServerPlayer player) {
        BlockPos spawn = level.getSharedSpawnPos();
        var standing = player.getDimensions(Pose.STANDING).makeBoundingBox(Vec3.ZERO);
        var server = level.getServer();
        if (level.dimensionType().hasSkyLight() && server.getWorldData().getGameType() != GameType.ADVENTURE) {
            int border = Mth.floor(level.getWorldBorder().getDistanceToBorder(spawn.getX(), spawn.getZ()));
            int radius = border <= 1 ? 1 : Mth.clamp(server.getSpawnRadius(level), 0, border);
            long width = radius * 2L + 1;
            int columns = (int) Math.min(Integer.MAX_VALUE, width * width);
            int stride = columns > 16 ? 17 : columns - 1;
            int offset = RandomSource.create().nextInt(columns);
            for (int i = 0; i < columns; i++) {
                long column = (offset + (long) stride * i) % columns;
                BlockPos candidate = PlayerRespawnLogicInvoker.thaumaturge$getOverworldRespawnPos(
                        level, spawn.getX() + (int) (column % width) - radius,
                        spawn.getZ() + (int) (column / width) - radius);
                if (candidate != null && level.noCollision(player, standing.move(Vec3.atBottomCenterOf(candidate)))) {
                    return candidate;
                }
            }
        }
        while (spawn.getY() < level.getMaxBuildHeight() - 1
                && !level.noCollision(player, standing.move(Vec3.atBottomCenterOf(spawn)))) {
            spawn = spawn.above();
        }
        while (spawn.getY() > level.getMinBuildHeight() + 1
                && level.noCollision(player, standing.move(Vec3.atBottomCenterOf(spawn.below())))) {
            spawn = spawn.below();
        }
        return spawn;
    }

    private static Optional<BlockPos> surface(ServerLevel level, BlockPos center, int maxRadius) {
        for (int radius = 0; radius <= maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    Optional<BlockPos> spot = SafeSpot.surface(level, center.getX() + dx, center.getZ() + dz);
                    if (spot.isPresent()) {
                        return spot;
                    }
                }
            }
        }
        return Optional.empty();
    }
}
