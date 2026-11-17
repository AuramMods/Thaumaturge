package com.leclowndu93150.thaumaturge.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

/** The 1.21.1 named effect-detail schema, independent of native effect IDs. */
public record EffectDetails(int amplifier, int duration, boolean ambient, boolean showParticles,
                            boolean showIcon, Optional<EffectDetails> hiddenEffect) {
    // Minecraft's UNSIGNED_BYTE reads through byteValue and writes a signed byte even in JsonOps.
    private static final Codec<Integer> AMPLIFIER = Codec.BYTE.xmap(Byte::toUnsignedInt, Integer::byteValue);
    public static final MapCodec<EffectDetails> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codecs.optionalFieldOf(AMPLIFIER, "amplifier", 0).forGetter(EffectDetails::amplifier),
            Codecs.optionalFieldOf(Codec.INT, "duration", 0).forGetter(EffectDetails::duration),
            Codecs.optionalFieldOf(Codec.BOOL, "ambient", false).forGetter(EffectDetails::ambient),
            Codecs.optionalFieldOf(Codec.BOOL, "show_particles", true).forGetter(EffectDetails::showParticles),
            Codecs.optionalFieldOf(Codec.BOOL, "show_icon").forGetter(d -> Optional.of(d.showIcon)),
            Codecs.optionalFieldOf(Codecs.lazyInitialized(EffectDetails::codec), "hidden_effect").forGetter(EffectDetails::hiddenEffect))
            .apply(i, (amplifier, duration, ambient, particles, icon, hidden) ->
                    new EffectDetails(amplifier, duration, ambient, particles, icon.orElse(particles), hidden)));

    public static Codec<EffectDetails> codec() { return MAP_CODEC.codec(); }
}
