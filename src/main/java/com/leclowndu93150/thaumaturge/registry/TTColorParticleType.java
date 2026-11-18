package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.content.particle.TTColorParticleOptions;

public final class TTColorParticleType extends TTParticleType<TTColorParticleOptions> {
    public TTColorParticleType() {
        super(TTColorParticleOptions::codec, TTColorParticleOptions::streamCodec);
    }
}
