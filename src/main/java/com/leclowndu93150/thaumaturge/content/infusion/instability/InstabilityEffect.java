package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.Codec;

public interface InstabilityEffect {
    Codec<InstabilityEffect> CODEC = Codecs.lazyInitialized(() -> Codecs.dispatch(TTInstabilityEffects.registry()
            .byNameCodec(), "type", InstabilityEffect::type, InstabilityEffectType::codec));

    InstabilityEffectType<?> type();

    void apply(InstabilityContext context);
}
