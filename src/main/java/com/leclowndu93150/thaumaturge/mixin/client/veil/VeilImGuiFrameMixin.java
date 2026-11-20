package com.leclowndu93150.thaumaturge.mixin.client.veil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "foundry.veil.impl.client.imgui.VeilImGuiImpl", remap = false)
abstract class VeilImGuiFrameMixin {
    @Shadow private boolean active;
    @Unique private int thaumaturge$nestedFrames;

    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$keepOuterFrameOpen(CallbackInfo ci) {
        // Minecraft can draw a loading screen before the outer render pass reaches its buffer swap.
        if (this.active) {
            this.thaumaturge$nestedFrames++;
            ci.cancel();
        }
    }

    @Inject(method = "endFrame", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$finishOnlyOwnFrame(CallbackInfo ci) {
        if (this.thaumaturge$nestedFrames > 0) {
            this.thaumaturge$nestedFrames--;
            ci.cancel();
        }
    }
}
