package com.leclowndu93150.thaumaturge.datamap.builtin;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;

public record FurnaceFuel(int burnTime) {
    public static final Codec<FurnaceFuel> BURN_TIME_CODEC = Codec.intRange(1, Integer.MAX_VALUE).xmap(FurnaceFuel::new, FurnaceFuel::burnTime);
    public static final Codec<FurnaceFuel> CODEC = Codecs.withAlternative(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("burn_time").xmap(FurnaceFuel::new, FurnaceFuel::burnTime).codec(), BURN_TIME_CODEC);
}
