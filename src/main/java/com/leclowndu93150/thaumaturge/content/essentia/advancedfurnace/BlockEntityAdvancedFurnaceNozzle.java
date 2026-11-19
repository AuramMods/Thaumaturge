package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Capability endpoint for a furnace mouth. The controller owns all stored essentia. */
public final class BlockEntityAdvancedFurnaceNozzle extends RegistryBlockEntity {
    public BlockEntityAdvancedFurnaceNozzle(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get(), pos, state);
        exposeConduit(side -> level == null ? null : AdvancedAlchemicalFurnaceStructure.nozzle(level, worldPosition, side));
    }
}
