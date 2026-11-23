package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public final class PechTrades {
    public record Entry(int tier, ItemStack stack) {}

    private PechTrades() {}

    public static List<Entry> tradesFor(int pechType, HolderLookup.Provider registries) {
        return registries.lookupOrThrow(PechTradeTable.REGISTRY_KEY).get(tableKey(pechType))
                .map(ref -> {
                    var configured = DataMaps.get(ref, PechTradeTable.DATA_MAP);
                    return (configured == null ? ref.value() : configured).trades().stream()
                            .map(trade -> new Entry(trade.tier(), trade.result().copy()))
                            .toList();
                })
                .orElse(List.of());
    }

    public static ResourceKey<PechTradeTable> tableKey(int pechType) {
        String table =
                switch (pechType) {
                    case EntityPech.TYPE_MAGE -> "mage";
                    case EntityPech.TYPE_STALKER -> "stalker";
                    default -> "forager";
                };
        return ResourceKey.create(PechTradeTable.REGISTRY_KEY, TTIds.rl(table));
    }
}
