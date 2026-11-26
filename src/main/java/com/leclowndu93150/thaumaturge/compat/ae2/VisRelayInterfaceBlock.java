package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.blockentity.networking.CableBusBlockEntity;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayCapabilities;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

final class VisRelayInterfaceBlock extends Block implements EntityBlock {
    VisRelayInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InterfaceEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InterfaceEntity(pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder loot) {
        return List.of(new ItemStack(this));
    }

    static final class InterfaceEntity extends RegistryBlockEntity implements IVisRelaySource {
        private boolean contactingSource;

        InterfaceEntity(BlockPos pos, BlockState state) {
            super(ThaumaturgeAe2.VIS_RELAY_INTERFACE_ENTITY.get(), pos, state);
            exposeCapability(VisRelayCapabilities.SOURCE, side -> side == null ? this : null);
        }

        @Override
        public boolean canSupply() {
            return withSource((tunnel, source) -> true, false);
        }

        @Override
        public int availableCentivis(ResourceKey<IAspect> primal) {
            return withSource((tunnel, source) -> source.availableCentivis(primal), 0);
        }

        @Override
        public int drainCentivis(ResourceKey<IAspect> primal, int amount, TransactionContext transaction) {
            return withSource((tunnel, source) -> tunnel.drain(source, primal, amount, transaction), 0);
        }

        private <T> T withSource(BiFunction<VisP2PTunnelPart, IVisRelaySource, T> action, T disconnected) {
            if (contactingSource || !(level instanceof ServerLevel server)) return disconnected;
            contactingSource = true;
            try {
                for (Direction direction : Direction.values()) {
                    var adjacent = worldPosition.relative(direction);
                    if (!LoadedChunks.contains(server, adjacent)) continue;
                    if (server.getBlockEntity(adjacent) instanceof CableBusBlockEntity cable
                            && cable.getPart(direction.getOpposite()) instanceof VisP2PTunnelPart tunnel) {
                        var source = tunnel.source();
                        if (source != null && source.canSupply()) return action.apply(tunnel, source);
                    }
                }
                return disconnected;
            } finally {
                contactingSource = false;
            }
        }
    }
}
