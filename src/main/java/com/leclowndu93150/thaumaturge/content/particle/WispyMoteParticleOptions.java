package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record WispyMoteParticleOptions(int color, int age, float gravity, int targetEntityId, boolean emissive)
        implements TTParticleOptions {

    public static final int NO_ENTITY = -1;

    public WispyMoteParticleOptions(int color, int age, float gravity, int targetEntityId) {
        this(color, age, gravity, targetEntityId, false);
    }

    public static final MapCodec<WispyMoteParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(WispyMoteParticleOptions::color),
                    Codec.INT.fieldOf("age").forGetter(WispyMoteParticleOptions::age),
                    Codec.FLOAT.fieldOf("gravity").forGetter(WispyMoteParticleOptions::gravity),
                    Codecs.optionalFieldOf(Codec.INT, "target", NO_ENTITY).forGetter(WispyMoteParticleOptions::targetEntityId),
                    Codecs.optionalFieldOf(Codec.BOOL, "emissive", false).forGetter(WispyMoteParticleOptions::emissive))
            .apply(inst, WispyMoteParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WispyMoteParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    WispyMoteParticleOptions::color,
                    ByteBufCodecs.VAR_INT,
                    WispyMoteParticleOptions::age,
                    ByteBufCodecs.FLOAT,
                    WispyMoteParticleOptions::gravity,
                    ByteBufCodecs.VAR_INT,
                    WispyMoteParticleOptions::targetEntityId,
                    ByteBufCodecs.BOOL,
                    WispyMoteParticleOptions::emissive,
                    WispyMoteParticleOptions::new);

    @Override
    public TTParticleType<WispyMoteParticleOptions> getType() {
        return TTParticles.WISPY_MOTE.get();
    }
}
