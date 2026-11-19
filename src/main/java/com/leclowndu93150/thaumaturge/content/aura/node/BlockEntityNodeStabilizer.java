package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlockEntityNodeStabilizer extends RegistryBlockEntity {
    public static final int MAX_COUNT = 37;
    public static final float BUBBLE_HALF = 0.72F;
    private static final float BUBBLE_LIFT = 1.5F;

    public int count;

    public BlockEntityNodeStabilizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.NODE_STABILIZER.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        Vec3 center = bubbleCenter();
        double boundsHalf = BUBBLE_HALF * Math.sqrt(2.0);
        return new AABB(getBlockPos()).minmax(new AABB(center, center).inflate(boundsHalf));
    }

    public Vec3 bubbleCenter() {
        return Vec3.atCenterOf(getBlockPos()).add(0.0, BUBBLE_LIFT - 0.5, 0.0);
    }

    public boolean isAdvanced() {
        return getBlockState().getBlock() instanceof BlockNodeStabilizer stabilizer && stabilizer.isAdvanced();
    }

    public void clientTick(Level clientLevel, BlockPos pos) {
        boolean active = clientLevel.getBlockEntity(pos.above()) instanceof BlockEntityNode node
                && !(node instanceof BlockEntityJarNode)
                && !clientLevel.hasNeighborSignal(pos);
        if (active) {
            if (count < MAX_COUNT) {
                count++;
            }
        } else if (count > 0) {
            count--;
        }
    }
}
