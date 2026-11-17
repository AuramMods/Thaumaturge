package com.leclowndu93150.thaumaturge.mixin.client.multiplayer;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerEffectMixin {
    @ModifyArg(method = "handleUpdateMobEffect", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/world/effect/MobEffect;IIZZZLnet/minecraft/world/effect/MobEffectInstance;Ljava/util/Optional;)V"), index = 2)
    private int thaumaturge$unsignedEffectAmplifier(int amplifier) {
        return amplifier & 255;
    }
}
