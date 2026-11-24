package com.leclowndu93150.thaumaturge.compat.auram;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jspecify.annotations.Nullable;

/** Client-tick positions shared by the two Auram voxel consumers. */
public final class AuramVisualSources {
    private static final boolean AVAILABLE = FMLLoader.getLoadingModList().getModFileById("auram") != null;
    private static final int MAX_CELLS = 256;
    private static final int ESSENTIA_LIGHT = 32;
    static final float ESSENTIA_VAPOR = 0.05F;
    static final Long2IntOpenHashMap LIGHTS = new Long2IntOpenHashMap();
    static final Long2FloatOpenHashMap RIFTS = new Long2FloatOpenHashMap();

    private static @Nullable Level world;
    private static long essentiaTick = -1;
    private static long riftTick = -1;

    private AuramVisualSources() {}

    public static boolean available() {
        return AVAILABLE;
    }

    public static void essentia(Level level, Vec3 point, int rgb, float opacity) {
        if (!AVAILABLE || opacity <= 0.0F) return;
        BlockPos pos = BlockPos.containing(point);
        if (!open(level, pos)) return;
        useWorld(level);
        long now = level.getGameTime();
        if (essentiaTick != now) {
            LIGHTS.clear();
            essentiaTick = now;
        }
        long key = pos.asLong();
        int intensity = Math.max(1, Math.round(Math.min(1.0F, opacity) * ESSENTIA_LIGHT));
        int previous = LIGHTS.get(key);
        if (intensity > (previous >>> 24) && (previous != 0 || LIGHTS.size() < MAX_CELLS)) {
            LIGHTS.put(key, intensity << 24 | (rgb & 0xFFFFFF));
        }
    }

    /** Returns false so a rift can retry if a selected point lies inside terrain. */
    public static boolean rift(Level level, Vec3 point) {
        if (!AVAILABLE) return false;
        BlockPos pos = BlockPos.containing(point);
        if (!open(level, pos)) return false;
        useWorld(level);
        long now = level.getGameTime();
        if (riftTick != now) {
            RIFTS.clear();
            riftTick = now;
        }
        long key = pos.asLong();
        if (RIFTS.size() >= MAX_CELLS && !RIFTS.containsKey(key)) return false;
        RIFTS.put(key, Math.min(1.0F, RIFTS.get(key) + 0.30F));
        return true;
    }

    static void discardExpired(Level level) {
        useWorld(level);
        long now = level.getGameTime();
        // Entity and particle ticks can straddle the client level clock increment.
        if (essentiaTick > now || essentiaTick < now - 1) LIGHTS.clear();
        if (riftTick > now || riftTick < now - 1) RIFTS.clear();
    }

    private static void useWorld(@Nullable Level level) {
        if (level == world) return;
        LIGHTS.clear();
        RIFTS.clear();
        essentiaTick = -1;
        riftTick = -1;
        world = level;
    }

    public static void clear(Level level) {
        if (level == world) useWorld(null);
    }

    private static boolean open(Level level, BlockPos pos) {
        if (!level.isClientSide() || level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos)) return false;
        var state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && !state.isCollisionShapeFullBlock(level, pos);
    }
}
