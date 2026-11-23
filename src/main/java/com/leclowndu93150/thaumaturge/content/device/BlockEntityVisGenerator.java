package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.blockentity.BlockCapabilityAccess;
import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.stage.OwnedProcessing;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.jspecify.annotations.Nullable;

public final class BlockEntityVisGenerator extends RegistryBlockEntity implements IEnergyStorage, OwnedProcessing {
    private static final int CAPACITY = 1000;
    private int energy;
    private @Nullable UUID owner;

    public BlockEntityVisGenerator(BlockPos pos, BlockState state) {
        super(TTBlockEntities.VIS_GENERATOR.get(), pos, state);
        exposeCapability(ForgeCapabilities.ENERGY, side -> side == outputFace() ? this : null);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVisGenerator generator) {
        if (!(level instanceof ServerLevel server) || !state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        var rules = AuraMachineRules.of(state.getBlock(), AuraMachineRules.VIS_GENERATOR,
                AuraMachineRules.VisGenerator.DEFAULT);
        if (generator.energy == 0) {
            if (!rules.enabled() || !AuraMachineRules.allows(
                    server, StageTarget.VIS_GENERATOR, state.getBlock(), generator.owner)) {
                return;
            }
            float drained = AuraHelper.drainVis(level, pos, rules.vis(), false);
            generator.energy = (int) (Math.min(1.0F, drained / rules.vis()) * rules.energy());
            generator.setChanged();
        }
        Direction facing = state.getValue(BlockStateProperties.FACING);
        IEnergyStorage target =
                BlockCapabilityAccess.find(level, pos.relative(facing), ForgeCapabilities.ENERGY, facing.getOpposite());
        if (target != null) {
            int pushed = target.receiveEnergy(Math.min(generator.energy, rules.maxPush()), false);
            if (pushed > 0) {
                generator.energy -= pushed;
                generator.setChanged();
            }
        }
    }

    public Direction outputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return CAPACITY;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        energy = input.getInt("energy");
        owner = input.hasUUID("Owner") ? input.getUUID("Owner") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt("energy", energy);
        if (owner != null) {
            output.putUUID("Owner", owner);
        }
    }
}
