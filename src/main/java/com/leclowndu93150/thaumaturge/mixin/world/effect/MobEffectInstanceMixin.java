package com.leclowndu93150.thaumaturge.mixin.world.effect;

import com.leclowndu93150.thaumaturge.serialization.EffectInstanceAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffectInstance.class)
abstract class MobEffectInstanceMixin implements EffectInstanceAccess {
    @Shadow public abstract int getAmplifier();

    @Override @Accessor("hiddenEffect")
    public abstract MobEffectInstance thaumaturge$hiddenEffect();

    @Inject(method = "writeDetailsTo", at = @At("RETURN"))
    private void thaumaturge$saveExtendedAmplifier(CompoundTag tag, CallbackInfo ci) {
        if (getAmplifier() > 127) tag.putInt("Amplifier", getAmplifier());
    }

    @ModifyVariable(method = "loadSpecifiedEffect", at = @At("STORE"), ordinal = 0)
    private static int thaumaturge$loadExtendedAmplifier(int original, MobEffect effect, CompoundTag tag) {
        // Keep legacy negative byte tags' native clamping; only our extended format uses an int.
        return tag.contains("Amplifier", Tag.TAG_INT) ? tag.getInt("Amplifier") : original;
    }
}
