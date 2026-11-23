package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record PechOffer(int value) {
    private static final int MAX_VALUE = 32;
    private static final Codec<PechOffer> CODEC = Codec.intRange(0, MAX_VALUE).fieldOf("value")
            .xmap(PechOffer::new, PechOffer::value).codec();

    public static final DataMapType<Item, PechOffer> DATA_MAP =
            DataMapType.builder(TTIds.rl("pech_offer"), Registries.ITEM, CODEC).build();

    public static int valueOf(ItemStack offer, HolderLookup.Provider registries) {
        if (offer.isEmpty()) {
            return 0;
        }
        PechOffer override = DataMaps.get(offer.getItemHolder(), DATA_MAP);
        if (override != null) {
            return override.value();
        }
        if (offer.is(Items.ENDER_PEARL)) {
            return 15;
        }
        int desire = AspectIndexAccess.of(offer).amountOf(TTAspects.DESIDERIUM, registries);
        return Math.min(MAX_VALUE, desire / 2);
    }
}
