package com.leclowndu93150.thaumaturge.content.essentia.crystalizer;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;

public record Crystallization(boolean enabled, int essentia, int progress, int crystals, Optional<ItemStack> result) {
    public static final int DEFAULT_PROGRESS = 200;
    public static final Crystallization DEFAULT = new Crystallization(true, 1, DEFAULT_PROGRESS, 1, Optional.empty());

    public static final Codec<Crystallization> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(Crystallization::enabled),
                    Codecs.optionalFieldOf(Codec.intRange(1, Integer.MAX_VALUE), "essentia", 1)
                            .forGetter(Crystallization::essentia),
                    Codecs.optionalFieldOf(Codec.intRange(1, Integer.MAX_VALUE), "progress", DEFAULT_PROGRESS)
                            .forGetter(Crystallization::progress),
                    Codecs.optionalFieldOf(Codec.intRange(1, 64), "crystals", 1).forGetter(Crystallization::crystals),
                    Codecs.optionalFieldOf(ItemStackCodecs.CODEC, "result").forGetter(Crystallization::result))
            .apply(i, Crystallization::new));

    public static final DataMapType<IAspect, Crystallization> DATA_MAP =
            DataMapType.builder(TTIds.rl("crystallization"), IAspect.REGISTRY_KEY, CODEC).build();

    public static Crystallization of(Holder<IAspect> input) {
        var override = DataMaps.get(input, DATA_MAP);
        return override == null ? DEFAULT : override;
    }

    ItemStack assemble(Holder<IAspect> input) {
        return result.map(ItemStack::copy).orElseGet(() -> EssentiaCrystalAccess.create(input, crystals));
    }
}
