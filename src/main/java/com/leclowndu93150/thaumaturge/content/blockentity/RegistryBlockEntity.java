package com.leclowndu93150.thaumaturge.content.blockentity;

import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Connect registry-aware mod persistence to Forge's native 1.20.1 callbacks. */
public abstract class RegistryBlockEntity extends BlockEntity {
    protected RegistryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    protected final HolderLookup.Provider registries() {
        return RegistryContext.resolve(level == null ? null : level.registryAccess());
    }

    @Override public final void load(CompoundTag input) {
        var registries = registries();
        RegistryContext.withRegistries(registries, () -> { loadAdditional(input, registries); return null; });
    }
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) { super.load(input); }

    @Override protected final void saveAdditional(CompoundTag output) {
        var registries = registries();
        RegistryContext.withRegistries(registries, () -> { saveAdditional(output, registries); return null; });
    }
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) { super.saveAdditional(output); }

    public final void loadWithComponents(CompoundTag input, HolderLookup.Provider registries) {
        RegistryContext.withRegistries(registries, () -> { load(input); return null; });
    }

    public final CompoundTag saveWithoutMetadata(HolderLookup.Provider registries) {
        return RegistryContext.withRegistries(registries, super::saveWithoutMetadata);
    }

    @Override public final CompoundTag getUpdateTag() {
        var registries = registries();
        return RegistryContext.withRegistries(registries, () -> getUpdateTag(registries));
    }
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return RegistryContext.withRegistries(registries, super::getUpdateTag);
    }

    @Override public final void handleUpdateTag(CompoundTag input) {
        var registries = registries();
        RegistryContext.withRegistries(registries, () -> { handleUpdateTag(input, registries); return null; });
    }
    public void handleUpdateTag(CompoundTag input, HolderLookup.Provider registries) {
        RegistryContext.withRegistries(registries, () -> { super.handleUpdateTag(input); return null; });
    }

    @Override public final void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) {
            var registries = registries();
            RegistryContext.withRegistries(registries, () -> { onDataPacket(connection, packet, registries); return null; });
        }
    }

    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        RegistryContext.withRegistries(registries, () -> { super.onDataPacket(connection, packet); return null; });
    }
}
