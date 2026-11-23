package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.itemdata.PotionData;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.alchemy.Potion;

public record SprayerRules(boolean enabled, Optional<AspectList> essentia, int maxCharges) {
    private static final int MAX_CHARGES = BlockEntityPotionSprayer.MAX_CHARGES;
    private static final SprayerRules DEFAULT = new SprayerRules(true, Optional.empty(), MAX_CHARGES);
    private static final Codec<AspectList> ESSENTIA = Codecs.validate(AspectList.CODEC, aspects ->
            aspects.entries().stream().mapToLong(entry -> entry.amount()).sum() <= 1000
                    ? DataResult.success(aspects)
                    : DataResult.error(() -> "A spray dose may cost at most 1000 essentia"));
    private static final Codec<SprayerRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(SprayerRules::enabled),
                    Codecs.optionalFieldOf(ESSENTIA, "essentia").forGetter(SprayerRules::essentia),
                    Codecs.optionalFieldOf(Codec.intRange(1, MAX_CHARGES), "max_charges", MAX_CHARGES)
                            .forGetter(SprayerRules::maxCharges))
            .apply(i, SprayerRules::new));

    public static final DataMapType<Potion, SprayerRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("potion_sprayer"), Registries.POTION, CODEC).build();

    static SprayerRules of(PotionData potion) {
        return potion.potion().map(holder -> DataMaps.get(holder, DATA_MAP)).orElse(DEFAULT);
    }
}
