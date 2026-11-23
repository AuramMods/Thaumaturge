package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

public record AltarRitualRules(boolean enabled, int eyes, Optional<Float> vis, Optional<Integer> channelTicks) {
    static final AltarRitualRules DEFAULT =
            new AltarRitualRules(true, BlockEntityEldritchAltar.MAX_EYES, Optional.empty(), Optional.empty());
    private static final Codec<AltarRitualRules> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(AltarRitualRules::enabled),
                    Codecs.optionalFieldOf(Codec.intRange(1, BlockEntityEldritchAltar.MAX_EYES), "eyes", DEFAULT.eyes())
                            .forGetter(AltarRitualRules::eyes),
                    Codecs.optionalFieldOf(Codec.floatRange(0.0F, 1000.0F), "vis").forGetter(AltarRitualRules::vis),
                    Codecs.optionalFieldOf(Codec.intRange(1, 1200), "channel_ticks")
                            .forGetter(AltarRitualRules::channelTicks))
            .apply(i, AltarRitualRules::new));

    public static final DataMapType<ObeliskSite, AltarRitualRules> DATA_MAP =
            DataMapType.builder(TTIds.rl("eldritch_ritual"), ObeliskSite.REGISTRY_KEY, CODEC).build();

    static AltarRitualRules of(HolderLookup.Provider registries, ResourceKey<ObeliskSite> site) {
        return registries.lookupOrThrow(ObeliskSite.REGISTRY_KEY).get(site)
                .map(holder -> DataMaps.get(holder, DATA_MAP)).orElse(DEFAULT);
    }
}
