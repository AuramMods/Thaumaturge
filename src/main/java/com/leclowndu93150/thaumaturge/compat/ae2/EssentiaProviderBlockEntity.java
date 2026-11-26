package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.storage.StorageHelper;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** One ME node serving either a sided tube connection or the infusion matrix. */
final class EssentiaProviderBlockEntity extends ThaumicGridBlockEntity
        implements IAspectSource, IEssentiaConduit {
    private static final int OUTLET_PULL = 8;

    EssentiaProviderBlockEntity(BlockPos pos, BlockState state, boolean infusion) {
        super((infusion ? ThaumaturgeAe2.INFUSION_PROVIDER_ENTITY : ThaumaturgeAe2.ESSENTIA_PROVIDER_ENTITY).get(),
                pos, state, 1.0);
        if (infusion) {
            exposeCapability(AspectCapabilities.CONTAINER, side -> this);
        } else {
            exposeConduit(side -> this);
        }
    }

    @Override
    public boolean isBlocked() {
        return grid() == null;
    }

    @Override
    public AspectList getAspects() {
        var grid = grid();
        if (grid == null) return AspectList.EMPTY;
        var values = new ArrayList<AspectInstance>();
        for (var entry : grid.getStorageService().getCachedInventory()) {
            if (!(entry.getKey() instanceof EssentiaKey key) || entry.getLongValue() <= 0) continue;
            var aspect = key.resolve(level);
            if (aspect != null) values.add(new AspectInstance(aspect, (int) Math.min(Integer.MAX_VALUE, entry.getLongValue())));
        }
        return AspectList.ofEntries(values);
    }

    @Override
    public void setAspects(AspectList aspects) {
        // This is a view of the network's storage, so changes must use fill or drain.
    }

    @Override
    public boolean accepts(Holder<IAspect> aspect) {
        var grid = grid();
        var key = EssentiaKey.of(aspect);
        return grid != null && key != null
                && grid.getStorageService().getInventory().insert(key, 1, Actionable.SIMULATE, source) > 0;
    }

    @Override
    public int fill(Holder<IAspect> aspect, int amount) {
        return amount - insert(aspect, amount, Direction.UP, false);
    }

    @Override
    public boolean drain(Holder<IAspect> aspect, int amount) {
        if (amount <= 0 || extract(aspect, amount, Direction.UP, true) != amount) return false;
        int extracted = extract(aspect, amount, Direction.UP, false);
        if (extracted == amount) return true;
        if (extracted > 0 && level instanceof ServerLevel server) {
            var key = EssentiaKey.of(aspect);
            var grid = grid();
            if (key != null) {
                long returned = grid == null ? 0 : grid.getStorageService().getInventory()
                        .insert(key, extracted, Actionable.MODULATE, source);
                key.dropAt(server, worldPosition, extracted - returned);
            }
        }
        return false;
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        var grid = grid();
        var key = EssentiaKey.of(aspect);
        if (grid == null || key == null || amount <= 0) return 0;
        return (int) StorageHelper.poweredInsert(grid.getEnergyService(), grid.getStorageService().getInventory(),
                key, amount, source, Actionable.ofSimulate(simulate));
    }

    @Override
    public int extract(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        var grid = grid();
        var key = EssentiaKey.of(aspect);
        if (grid == null || key == null || amount <= 0) return 0;
        return (int) StorageHelper.poweredExtraction(grid.getEnergyService(), grid.getStorageService().getInventory(),
                key, amount, source, Actionable.ofSimulate(simulate));
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        if (!(level instanceof ServerLevel server) || isBlocked()) return null;
        if (face != null) {
            BlockPos adjacent = worldPosition.relative(face);
            if (!LoadedChunks.contains(server, adjacent)) return null;
            var receivingFace = face.getOpposite();
            var receiver = EssentiaAccess.conduit(server, adjacent, receivingFace);
            if (receiver == null || !receiver.receivesOn(receivingFace)) return null;
            var demand = receiver.pull(receivingFace);
            if (demand.strength() <= OUTLET_PULL) return null;
            var wanted = demand.aspect();
            if (wanted != null) return extract(wanted, 1, face, true) > 0 ? wanted : null;
        }
        var entries = getAspects().entries();
        return entries.isEmpty() ? null : entries.get(0).aspect();
    }

    @Override
    public int heldAmount(@Nullable Direction face) {
        var aspect = heldAspect(face);
        return aspect == null ? 0 : amountOf(aspect);
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        return grid() != null ? EssentiaPull.any(OUTLET_PULL) : EssentiaPull.NONE;
    }

    @Override
    public int pullThreshold() {
        return OUTLET_PULL;
    }

    @Override
    public boolean connectsOn(Direction face) {
        return true;
    }

    @Override
    public boolean receivesOn(Direction face) {
        return !isBlocked();
    }

    @Override
    public boolean releasesOn(Direction face) {
        return !isBlocked();
    }
}
