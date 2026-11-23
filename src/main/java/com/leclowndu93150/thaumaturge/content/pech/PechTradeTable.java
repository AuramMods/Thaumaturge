package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record PechTradeTable(List<PechTrade> trades) {
    public static final ResourceKey<Registry<PechTradeTable>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("thaumaturge", "pech_trade"));

    public static final Codec<PechTradeTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    PechTrade.CODEC.listOf().fieldOf("trades").forGetter(PechTradeTable::trades))
            .apply(instance, PechTradeTable::new));

    public static final DataMapType<PechTradeTable, PechTradeTable> DATA_MAP =
            DataMapType.builder(TTIds.rl("pech_trades"), REGISTRY_KEY, CODEC).build();

    public record PechTrade(int tier, ItemStack result) {
        public static final Codec<PechTrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.intRange(1, 5).fieldOf("tier").forGetter(PechTrade::tier),
                        ItemStackCodecs.CODEC.fieldOf("result").forGetter(PechTrade::result))
                .apply(instance, PechTrade::new));
    }
}
