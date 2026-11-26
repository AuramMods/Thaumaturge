package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

abstract class ThaumicGridBlockEntity extends RegistryBlockEntity implements IInWorldGridNodeHost, IActionHost {
    protected final IManagedGridNode node;
    protected final IActionSource source = IActionSource.ofMachine(this);

    ThaumicGridBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, double idlePower) {
        super(type, pos, state);
        node = GridHelper.<ThaumicGridBlockEntity>createManagedNode(this, (owner, ignored) -> owner.setChanged())
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(idlePower)
                .setVisualRepresentation(state.getBlock().asItem())
                .setInWorldNode(true)
                .setTagName("grid")
                .setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    void setOwner(ServerPlayer player) {
        node.setOwningPlayerId(appeng.api.features.IPlayerRegistry.getPlayerId(player));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) {
            GridHelper.onFirstTick(this, owner -> {
                if (!owner.isRemoved() && !owner.node.isReady()) owner.node.create(owner.level, owner.worldPosition);
            });
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        node.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        node.destroy();
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        node.loadFromNBT(input);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        node.saveToNBT(output);
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction side) {
        return node.getNode();
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return node.getNode();
    }

    protected @Nullable IGrid grid() {
        return level instanceof ServerLevel && !isRemoved() && node.isActive() ? node.getGrid() : null;
    }
}
