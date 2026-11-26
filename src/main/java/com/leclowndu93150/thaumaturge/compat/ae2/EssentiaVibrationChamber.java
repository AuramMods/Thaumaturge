package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaTransfer;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.SinkOnlyEssentiaFace;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jspecify.annotations.Nullable;

final class EssentiaVibrationChamber extends Block implements EntityBlock {
    private static final IntegerProperty BURNING = IntegerProperty.create("burning", 0, 2);

    EssentiaVibrationChamber(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BURNING, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BURNING);
    }

    @Override
    public ChamberEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChamberEntity(pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder loot) {
        return List.of(new ItemStack(this));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof ChamberEntity chamber) {
            chamber.dropFuel(server);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    private enum Fuel {
        IGNIS(TTAspects.IGNIS, 5, 800),
        POTENTIA(TTAspects.POTENTIA, 8, 1000);

        private final ResourceKey<IAspect> aspect;
        private final double rate;
        private final double energy;

        Fuel(ResourceKey<IAspect> aspect, double rate, int ticks) {
            this.aspect = aspect;
            this.rate = rate;
            this.energy = rate * ticks;
        }

        private static @Nullable Fuel from(Holder<IAspect> aspect) {
            for (var fuel : ChamberEntity.FUELS) {
                if (aspect.is(fuel.aspect)) return fuel;
            }
            return null;
        }
    }

    static final class ChamberEntity extends ThaumicGridBlockEntity implements IGridTickable, SinkOnlyEssentiaFace {
        private static final Fuel[] FUELS = Fuel.values();
        private static final int CAPACITY = 16;
        private static final int SUCTION = 128;
        private final int[] reserve = new int[FUELS.length];
        private @Nullable Fuel burning;
        private double energyLeft;

        ChamberEntity(BlockPos pos, BlockState state) {
            super(ThaumaturgeAe2.ESSENTIA_VIBRATION_CHAMBER_ENTITY.get(), pos, state, 0);
            node.setFlags().addService(IGridTickable.class, this);
            exposeConduit(side -> side == null || connectsOn(side) ? this : null);
        }

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(1, 10, false, false);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode gridNode, int ticks) {
            if (!(level instanceof ServerLevel server)) return TickRateModulation.IDLE;
            var energy = gridNode.getGrid().getEnergyService();
            if (energy.injectPower(1, Actionable.SIMULATE) < 1) {
                if (energyLeft <= 0) {
                    drawFuel(server);
                    for (var fuel : FUELS) {
                        if (reserve[fuel.ordinal()] <= 0) continue;
                        reserve[fuel.ordinal()]--;
                        burning = fuel;
                        energyLeft = fuel.energy;
                        break;
                    }
                }
                if (burning != null && energyLeft > 0) {
                    double offer = Math.min(energyLeft, burning.rate * ticks);
                    double accepted = offer - energy.injectPower(offer, Actionable.MODULATE);
                    if (accepted > 0) {
                        energyLeft -= accepted;
                        setChanged();
                        showBurning(server, burning.ordinal() + 1);
                        return TickRateModulation.FASTER;
                    }
                }
            }
            showBurning(server, 0);
            return TickRateModulation.IDLE;
        }

        private void showBurning(ServerLevel level, int state) {
            if (getBlockState().getValue(BURNING) != state) {
                level.setBlock(worldPosition, getBlockState().setValue(BURNING, state), Block.UPDATE_CLIENTS);
            }
        }

        private void drawFuel(ServerLevel server) {
            if (room() <= 0) return;
            for (Direction side : Direction.values()) {
                if (!connectsOn(side)) continue;
                Direction opposite = side.getOpposite();
                var supplier = EssentiaFlowHandler.conduit(server, worldPosition.relative(side), opposite);
                if (supplier == null || !supplier.connectsOn(opposite) || !supplier.releasesOn(opposite)
                        || supplier.pull(opposite).strength() >= SUCTION || supplier.pullThreshold() > SUCTION) continue;
                for (var fuel : FUELS) {
                    var aspect = Aspects.resolve(server, fuel.aspect);
                    if (aspect != null && EssentiaTransfer.transfer(supplier, opposite, this, side, aspect, 1, false) > 0) {
                        return;
                    }
                }
            }
        }

        private int room() {
            return CAPACITY - reserve[0] - reserve[1];
        }

        @Override
        public int insert(Holder<IAspect> aspect, int amount, Direction side, boolean simulate) {
            var fuel = Fuel.from(aspect);
            if (fuel == null || !(level instanceof ServerLevel) || !receivesOn(side) || amount <= 0) return 0;
            int accepted = Math.min(amount, room());
            if (accepted > 0 && !simulate) {
                reserve[fuel.ordinal()] += accepted;
                setChanged();
            }
            return accepted;
        }

        @Override
        public int roomFor(Holder<IAspect> aspect, Direction side) {
            return connectsOn(side) && Fuel.from(aspect) != null ? room() : 0;
        }

        @Override public boolean connectsOn(Direction side) { return side != Direction.NORTH; }
        @Override public boolean receivesOn(Direction side) { return connectsOn(side) && room() > 0; }

        @Override
        public EssentiaPull pull(@Nullable Direction side) {
            if (side == Direction.NORTH || room() <= 0) return EssentiaPull.NONE;
            return EssentiaPull.any(SUCTION);
        }

        private void dropFuel(ServerLevel level) {
            for (var fuel : FUELS) {
                var aspect = Aspects.resolve(level, fuel.aspect);
                if (aspect != null) {
                    var key = EssentiaKey.of(aspect);
                    if (key != null) key.dropAt(level, worldPosition, reserve[fuel.ordinal()]);
                }
            }
        }

        @Override
        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            java.util.Arrays.fill(reserve, 0);
            var saved = tag.getIntArray("fuel");
            for (int i = 0; i < Math.min(saved.length, reserve.length); i++) {
                reserve[i] = Mth.clamp(saved[i], 0, room());
            }
            int index = tag.getInt("burning");
            burning = index > 0 && index <= FUELS.length ? FUELS[index - 1] : null;
            energyLeft = burning == null ? 0 : Mth.clamp(tag.getDouble("energy"), 0, burning.energy);
        }

        @Override
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            tag.putIntArray("fuel", reserve);
            tag.putInt("burning", burning == null ? 0 : burning.ordinal() + 1);
            tag.putDouble("energy", energyLeft);
        }
    }
}
