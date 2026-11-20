package com.leclowndu93150.thaumaturge.mixin.client.veil;

import org.lwjgl.system.Platform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "foundry.veil.impl.client.imgui.VeilImGuiImpl", remap = false)
abstract class VeilImGuiNativesMixin {
    @Inject(method = "setImGuiPath", at = @At("HEAD"), cancellable = true)
    private static void thaumaturge$keepPlatformImGuiLibrary(CallbackInfo ci) {
        // Veil's ARM override names a macOS library even when Java runs on Linux.
        if (Platform.get() != Platform.MACOSX || System.getProperty("imgui.library.name") != null) {
            ci.cancel();
        }
    }
}
