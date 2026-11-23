package com.leclowndu93150.thaumaturge.content.research.decon;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record DeconstructionRules(boolean enabled, Optional<Holder<IAspect>> result, int points, Optional<Float> chance) {
    private static final DeconstructionRules DEFAULT =
            new DeconstructionRules(true, Optional.empty(), 1, Optional.empty());
    private static final Codec<DeconstructionRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(DeconstructionRules::enabled),
                    Codecs.optionalFieldOf(RegistryFixedCodec.create(IAspect.REGISTRY_KEY), "result")
                            .forGetter(DeconstructionRules::result),
                    Codecs.optionalFieldOf(Codec.intRange(1, 64), "points", 1).forGetter(DeconstructionRules::points),
                    Codecs.optionalFieldOf(Codec.floatRange(0.0F, 1.0F), "chance").forGetter(DeconstructionRules::chance))
            .apply(i, DeconstructionRules::new));

    public static final DataMapType<Item, DeconstructionRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("deconstruction"), Registries.ITEM, CODEC).synced(CODEC, false).build();

    public static DeconstructionRules of(ItemStack input) {
        return Optional.ofNullable(DataMaps.get(input.getItemHolder(), DATA_MAP)).orElse(DEFAULT);
    }
}
