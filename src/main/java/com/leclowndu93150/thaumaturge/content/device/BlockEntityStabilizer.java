package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.compat.Java17;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.stage.OwnedProcessing;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class BlockEntityStabilizer extends AbstractSyncedBlockEntity implements OwnedProcessing {
    public static final int MAX_CHARGE = 15;

    private static final int COOLDOWN_PER_RIFT = 5;
    private static final String CHARGE_KEY = "energy";

    private int ticks;
    private int cooldown;
    private int charge;
    private @Nullable UUID owner;

    public BlockEntityStabilizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.STABILIZER.get(), pos, state);
    }

    public int charge() {
        return charge;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    private StabilizerRules rules() {
        return AuraMachineRules.of(getBlockState().getBlock(), StabilizerRules.DATA_MAP, StabilizerRules.DEFAULT);
    }

    private boolean allowed(@Nullable Level currentLevel, ResourceKey<?>... subjects) {
        return !(currentLevel instanceof ServerLevel server) || AuraMachineRules.allows(server,
                StageTarget.STABILIZER, getBlockState().getBlock(), owner, subjects);
    }

    public boolean spendForShield(int cost) {
        if (!rules().enabled() || !allowed(level)) {
            return false;
        }
        if (cost <= 0) {
            return true;
        }
        if (charge < cost) {
            return false;
        }
        changeCharge(charge - cost);
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityStabilizer stabilizer) {
        stabilizer.work(level);
    }

    private void work(Level level) {
        ticks++;
        if (cooldown > 0) {
            cooldown--;
        }
        StabilizerRules rules = rules();
        if (!rules.enabled()) {
            return;
        }
        if (ticks % rules.chargeInterval() == 0 && charge < MAX_CHARGE && allowed(level)) {
            changeCharge(charge + 1);
            AuraHelper.polluteAura(level, worldPosition, rules.fluxPerCharge(), true);
        }
        if (ticks % rules.pulseInterval() == 0 && charge >= rules.pulseCost() && cooldown <= 0
                && allowed(level, TTEntities.FLUX_RIFT.get().builtInRegistryHolder().key())) {
            steadyRifts(level, rules);
        }
    }

    private void steadyRifts(Level level, StabilizerRules rules) {
        AABB reach = LevelSpaces.worldBox(level, worldPosition, new AABB(worldPosition).inflate(rules.reach()));
        int remaining = charge;
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, reach)) {
            if (remaining < rules.pulseCost()) {
                break;
            }
            if (rift.stabilityTier() == EntityFluxRift.Stability.VERY_STABLE) {
                continue;
            }
            remaining -= rules.pulseCost();
            rift.nudgeStability();
            cooldown += COOLDOWN_PER_RIFT;
        }
        if (remaining != charge) {
            changeCharge(remaining);
        }
    }

    private void changeCharge(int next) {
        int clamped = Java17.clamp(next, 0, MAX_CHARGE);
        if (clamped == charge) {
            return;
        }
        charge = clamped;
        setChangedAndSync();
        if (level != null && !level.isClientSide()) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            refreshInlays(level);
        }
    }

    private void refreshInlays(Level level) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = worldPosition.relative(side);
            if (LoadedChunks.contains(level, neighbour) && BlockInlay.chargeAt(level, neighbour) >= 0) {
                BlockInlay.updateNetwork(level, neighbour);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        charge = Java17.clamp(input.getInt(CHARGE_KEY), 0, MAX_CHARGE);
        owner = input.hasUUID("Owner") ? input.getUUID("Owner") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt(CHARGE_KEY, charge);
        if (owner != null) output.putUUID("Owner", owner);
    }

    @Override
    protected void writeClientData(CompoundTag output, HolderLookup.Provider registries) {
        super.writeClientData(output, registries);
        output.remove("Owner");
    }
}
