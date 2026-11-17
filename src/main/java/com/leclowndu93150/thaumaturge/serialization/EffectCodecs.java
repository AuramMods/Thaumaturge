package com.leclowndu93150.thaumaturge.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/** Named 1.21.1 effect data and constructors backed by real Forge effect instances. */
public final class EffectCodecs {
    private EffectCodecs() {}
    public static final Codec<Holder<MobEffect>> MOB_EFFECT = BuiltInRegistries.MOB_EFFECT.holderByNameCodec();
    private record Value(Holder<MobEffect> id, EffectDetails details) {}
    private static final Codec<Value> VALUE_CODEC = RecordCodecBuilder.create(i -> i.group(
            MOB_EFFECT.fieldOf("id").forGetter(Value::id),
            EffectDetails.MAP_CODEC.forGetter(Value::details)).apply(i, Value::new));
    public static final Codec<MobEffectInstance> CODEC = VALUE_CODEC.flatXmap(
            value -> DataResult.success(fromDetails(value.id.value(), value.details)),
            effect -> {
                try { return DataResult.success(new Value(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.getEffect()), details(effect))); }
                catch (IllegalArgumentException failure) { return DataResult.error(failure::getMessage); }
            });

    public static EffectDetails details(MobEffectInstance effect) {
        MobEffectInstance hidden = ((EffectInstanceAccess) effect).thaumaturge$hiddenEffect();
        if (hidden != null && hidden.getEffect() != effect.getEffect()) {
            throw new IllegalArgumentException("Hidden effect must use the parent effect type");
        }
        return new EffectDetails(effect.getAmplifier(), effect.getDuration(), effect.isAmbient(), effect.isVisible(), effect.showIcon(),
                Optional.ofNullable(hidden).map(EffectCodecs::details));
    }

    public static MobEffectInstance fromDetails(MobEffect effect, EffectDetails details) {
        return new MobEffectInstance(effect, details.duration(), Mth.clamp(details.amplifier(), 0, 255), details.ambient(), details.showParticles(),
                details.showIcon(), details.hiddenEffect().map(hidden -> fromDetails(effect, hidden)).orElse(null), effect.createFactorData());
    }

    public static MobEffectInstance instance(MobEffect effect) { return instance(effect, 0); }
    public static MobEffectInstance instance(MobEffect effect, int duration) { return instance(effect, duration, 0); }
    public static MobEffectInstance instance(MobEffect effect, int duration, int amplifier) {
        return instance(effect, duration, amplifier, false, true, true);
    }
    public static MobEffectInstance instance(MobEffect effect, int duration, int amplifier, boolean ambient, boolean visible) {
        return instance(effect, duration, amplifier, ambient, visible, visible);
    }
    public static MobEffectInstance instance(MobEffect effect, int duration, int amplifier, boolean ambient, boolean visible, boolean icon) {
        return new MobEffectInstance(effect, duration, Mth.clamp(amplifier, 0, 255), ambient, visible, icon);
    }
    public static MobEffectInstance instance(Holder<MobEffect> effect) { return instance(effect.value()); }
    public static MobEffectInstance instance(Holder<MobEffect> effect, int duration) { return instance(effect.value(), duration); }
    public static MobEffectInstance instance(Holder<MobEffect> effect, int duration, int amplifier) { return instance(effect.value(), duration, amplifier); }
    public static MobEffectInstance instance(Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible) {
        return instance(effect.value(), duration, amplifier, ambient, visible);
    }
    public static MobEffectInstance instance(Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible, boolean icon) {
        return instance(effect.value(), duration, amplifier, ambient, visible, icon);
    }
    public static MobEffectInstance instance(MobEffectInstance source) { return new MobEffectInstance(source); }
}
