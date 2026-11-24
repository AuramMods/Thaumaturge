package com.leclowndu93150.thaumaturge.compat.auram;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

/** Adapter for the sparse local sources and dynamic light volume in AuramCore 1.0.40. */
public final class AuramVoxelBridge {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FIELD_PACKAGE = "art.arcane.auram.field.VoxelGrid";
    private static final String VAPOR_PACKAGE = "art.arcane.auram.vapor.client.";
    private static Grid grid;
    private static Vapor vapor;
    private static boolean lightUnavailable;
    private static boolean vaporUnavailable;

    private AuramVoxelBridge() {}

    public static void addLights(ClientLevel level, Object origin, Int2IntOpenHashMap colors) {
        AuramVisualSources.discardExpired(level);
        if (AuramVisualSources.LIGHTS.isEmpty() || lightUnavailable) return;
        try {
            var window = grid().window(origin);
            var lights = AuramVisualSources.LIGHTS.long2IntEntrySet().fastIterator();
            while (lights.hasNext()) {
                var entry = lights.next();
                int index = window.index(BlockPos.of(entry.getLongKey()));
                if (index >= 0) colors.putIfAbsent(index, entry.getIntValue());
            }
        } catch (ReflectiveOperationException | RuntimeException failure) {
            lightUnavailable = true;
            LOGGER.warn("Thaumaturge could not access Auram's dynamic voxel lighting", failure);
        }
    }

    public static Object addVapor(ClientLevel level, BlockPos camera, Object ambient) {
        AuramVisualSources.discardExpired(level);
        var essentia = AuramVisualSources.LIGHTS;
        var rifts = AuramVisualSources.RIFTS;
        if (vaporUnavailable || (essentia.isEmpty() && rifts.isEmpty())) return ambient;
        try {
            var access = vapor();
            var window = grid().around(camera);
            List<Object> additions = new ArrayList<>();
            var essentiaCells = essentia.keySet().iterator();
            while (essentiaCells.hasNext()) {
                long cell = essentiaCells.nextLong();
                if (rifts.containsKey(cell)) continue;
                appendVapor(additions, access, window, cell, AuramVisualSources.ESSENTIA_VAPOR);
            }
            var riftCells = rifts.long2FloatEntrySet().fastIterator();
            while (riftCells.hasNext()) {
                var entry = riftCells.next();
                long cell = entry.getLongKey();
                float rate = entry.getFloatValue();
                if (essentia.containsKey(cell)) rate = Math.min(1.0F, rate + AuramVisualSources.ESSENTIA_VAPOR);
                appendVapor(additions, access, window, cell, rate);
            }
            if (additions.isEmpty()) return ambient;
            var existing = (List<?>) access.continuous().invoke(ambient);
            var combined = new ArrayList<Object>(existing.size() + additions.size());
            combined.addAll(existing);
            combined.addAll(additions);
            return access.ambient().newInstance(access.puffs().invoke(ambient), combined);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            vaporUnavailable = true;
            LOGGER.warn("Thaumaturge could not access Auram's local Vapor sources", failure);
            return ambient;
        }
    }

    private static void appendVapor(List<Object> additions, Vapor access, Window window, long cell, float rate)
            throws ReflectiveOperationException {
        BlockPos pos = BlockPos.of(cell);
        if (window.index(pos) < 0) return;
        Object position = access.position().newInstance(pos.getX(), pos.getY(), pos.getZ());
        additions.add(access.source().newInstance(position, rate, 0));
    }

    private static Grid grid() throws ReflectiveOperationException {
        if (grid == null) {
            Class<?> type = Class.forName(FIELD_PACKAGE);
            Class<?> position = Class.forName(FIELD_PACKAGE + "$Position");
            Object blocks = type.getField("BLOCKS").get(null);
            grid = new Grid((int) type.getMethod("side").invoke(blocks),
                    type.getMethod("originAround", int.class, int.class, int.class),
                    position.getMethod("x"), position.getMethod("y"), position.getMethod("z"));
        }
        return grid;
    }

    private static Vapor vapor() throws ReflectiveOperationException {
        if (vapor == null) {
            Class<?> position = Class.forName(FIELD_PACKAGE + "$Position");
            Class<?> source = Class.forName(VAPOR_PACKAGE + "VaporVolume$Source");
            Class<?> ambient = Class.forName(VAPOR_PACKAGE + "VaporAmbient$Sources");
            vapor = new Vapor(position.getConstructor(int.class, int.class, int.class),
                    constructor(source, position, float.class, int.class),
                    constructor(ambient, List.class, List.class), getter(ambient, "puffs"), getter(ambient, "burning"));
        }
        return vapor;
    }

    private static Constructor<?> constructor(Class<?> type, Class<?>... arguments) throws ReflectiveOperationException {
        var result = type.getDeclaredConstructor(arguments);
        result.setAccessible(true);
        return result;
    }

    private static Method getter(Class<?> type, String name) throws ReflectiveOperationException {
        var result = type.getDeclaredMethod(name);
        result.setAccessible(true);
        return result;
    }

    private record Vapor(Constructor<?> position, Constructor<?> source, Constructor<?> ambient,
                         Method puffs, Method continuous) {}

    private record Grid(int side, Method originAround, Method x, Method y, Method z) {
        Window window(Object origin) throws ReflectiveOperationException {
            return new Window((int) x.invoke(origin), (int) y.invoke(origin), (int) z.invoke(origin), side);
        }

        Window around(BlockPos camera) throws ReflectiveOperationException {
            return window(originAround.invoke(null, camera.getX(), camera.getY(), camera.getZ()));
        }
    }

    private record Window(int x, int y, int z, int side) {
        int index(BlockPos pos) {
            int dx = pos.getX() - x, dy = pos.getY() - y, dz = pos.getZ() - z;
            if (dx < 0 || dx >= side || dy < 0 || dy >= side || dz < 0 || dz >= side) return -1;
            return dx + side * (dy + side * dz);
        }
    }
}
