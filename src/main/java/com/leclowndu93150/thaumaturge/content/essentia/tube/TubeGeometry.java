package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TubeGeometry {
    public static final int CORE_HIT = 6;

    private static final double PIXELS = 16.0;
    private static final double CENTER = 8.0;
    private static final double SURFACE_EPSILON = 1.0E-4;
    private static final double PIPE_CORE_HALF = 2.0;
    private static final double FILTER_CORE_HALF = 3.0;
    private static final double BUFFER_CORE_HALF = 4.0;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int CONNECTION_MASK_COUNT = 1 << DIRECTIONS.length;
    private static final VoxelShape FLANGE = Block.box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0);
    private static final VoxelShape ARM = Shapes.or(FLANGE, Block.box(6.0, 2.0, 6.0, 10.0, 6.0, 10.0));
    private static final VoxelShape RESTRICTED_ARM = Shapes.or(FLANGE, Block.box(7.0, 2.0, 7.0, 9.0, 6.0, 9.0));
    private static final VoxelShape INLET_MARKER = Block.box(5.0, 3.0, 5.0, 11.0, 5.0, 11.0);

    public static final TubeGeometry PIPE = new TubeGeometry(PIPE_CORE_HALF, ARM, Shapes.empty());
    public static final TubeGeometry RESTRICTED = new TubeGeometry(PIPE_CORE_HALF, RESTRICTED_ARM, Shapes.empty());
    public static final TubeGeometry FILTER = new TubeGeometry(FILTER_CORE_HALF, ARM, Shapes.empty());
    public static final TubeGeometry BUFFER = new TubeGeometry(BUFFER_CORE_HALF, ARM, Shapes.empty());
    public static final TubeGeometry ONEWAY = new TubeGeometry(PIPE_CORE_HALF, ARM, INLET_MARKER);

    private final double coreHalf;
    private final VoxelShape[] baseShapes = new VoxelShape[CONNECTION_MASK_COUNT];
    private final VoxelShape[][] inletShapes;

    private TubeGeometry(double coreHalf, VoxelShape downArm, VoxelShape downInletMarker) {
        this.coreHalf = coreHalf;
        VoxelShape core = Block.box(
                CENTER - coreHalf,
                CENTER - coreHalf,
                CENTER - coreHalf,
                CENTER + coreHalf,
                CENTER + coreHalf,
                CENTER + coreHalf);
        Map<Direction, VoxelShape> arms = DeviceShapes.facingShapesFromDown(downArm);
        Map<Direction, VoxelShape> inletMarkers = DeviceShapes.facingShapesFromDown(downInletMarker);
        inletShapes = downInletMarker.isEmpty()
                ? new VoxelShape[0][]
                : new VoxelShape[DIRECTIONS.length][CONNECTION_MASK_COUNT];

        // Forge rebuilds its block-state caches on tag reload. Reuse geometry for the same connections.
        for (int mask = 0; mask < CONNECTION_MASK_COUNT; mask++) {
            VoxelShape shape = core;
            for (Direction direction : DIRECTIONS) {
                if ((mask & connectionBit(direction)) != 0) {
                    shape = Shapes.or(shape, arms.get(direction));
                }
            }
            baseShapes[mask] = shape.optimize();
            for (int inletIndex = 0; inletIndex < inletShapes.length; inletIndex++) {
                Direction inlet = DIRECTIONS[inletIndex];
                inletShapes[inletIndex][mask] = (mask & connectionBit(inlet)) != 0
                        ? Shapes.or(shape, inletMarkers.get(inlet)).optimize()
                        : baseShapes[mask];
            }
        }
    }

    public VoxelShape shape(BlockState state) {
        int mask = 0;
        for (Direction direction : DIRECTIONS) {
            if (state.getValue(BlockEssentiaTransport.propertyFor(direction))) {
                mask |= connectionBit(direction);
            }
        }
        if (inletShapes.length > 0 && state.hasProperty(BlockStateProperties.FACING)) {
            Direction inlet = state.getValue(BlockStateProperties.FACING).getOpposite();
            return inletShapes[inlet.ordinal()][mask];
        }
        return baseShapes[mask];
    }

    private static int connectionBit(Direction direction) {
        return 1 << direction.ordinal();
    }

    public int subHit(Vec3 local) {
        double dx = local.x - CENTER / PIXELS;
        double dy = local.y - CENTER / PIXELS;
        double dz = local.z - CENTER / PIXELS;
        double limit = coreHalf / PIXELS + SURFACE_EPSILON;
        if (Math.abs(dx) <= limit && Math.abs(dy) <= limit && Math.abs(dz) <= limit) {
            return CORE_HIT;
        }
        return Direction.getNearest((float) dx, (float) dy, (float) dz).ordinal();
    }
}
