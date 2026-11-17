package com.leclowndu93150.thaumaturge.serialization;

import net.minecraft.world.effect.MobEffectInstance;

/** Native hidden effect, exposed by the common effect mixin. */
public interface EffectInstanceAccess {
    MobEffectInstance thaumaturge$hiddenEffect();
}
