package com.leclowndu93150.thaumaturge.mixin.client.iris;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pathways.HandRenderer", remap = false)
public abstract class HandRendererMixin {
    @Inject(
            method = "isHandTranslucent(Lnet/minecraft/world/InteractionHand;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void thaumaturge$translucentLens(InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getItemInHand(hand).is(TTItems.THAUMOMETER.get())) {
            cir.setReturnValue(true);
        }
    }
}
