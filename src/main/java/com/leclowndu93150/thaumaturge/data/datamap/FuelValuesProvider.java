package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import com.leclowndu93150.thaumaturge.datamap.DataMapProvider;
import com.leclowndu93150.thaumaturge.datamap.builtin.FurnaceFuel;
import com.leclowndu93150.thaumaturge.datamap.builtin.BuiltinDataMaps;

public final class FuelValuesProvider extends DataMapProvider {
    private static final int ALUMENTUM_BURN_TICKS = 4800;

    public FuelValuesProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<FurnaceFuel, Item> b = builder(BuiltinDataMaps.FURNACE_FUELS);

        b.add(TTItems.ALUMENTUM, new FurnaceFuel(ALUMENTUM_BURN_TICKS), false);
    }

    @Override
    public String getName() {
        return "Thaumaturge Fuel Values Data Map";
    }
}
