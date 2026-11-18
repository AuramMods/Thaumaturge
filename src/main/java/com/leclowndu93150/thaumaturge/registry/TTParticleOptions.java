package com.leclowndu93150.thaumaturge.registry;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;

/** Native callbacks for Thaumaturge's codec-backed particle options. */
public interface TTParticleOptions extends ParticleOptions {
    @Override
    TTParticleType<?> getType();

    @Override
    default void writeToNetwork(FriendlyByteBuf buffer) {
        getType().write(buffer, this);
    }

    @Override
    default String writeToString() {
        return getType().commandString(this);
    }
}
