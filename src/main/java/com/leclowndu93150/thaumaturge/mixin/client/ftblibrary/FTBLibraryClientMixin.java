package com.leclowndu93150.thaumaturge.mixin.client.ftblibrary;

import com.leclowndu93150.thaumaturge.client.screen.casters.focal.FocalManipulatorScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.ftb.mods.ftblibrary.FTBLibraryClient", remap = false)
abstract class FTBLibraryClientMixin {
    @Inject(method = "areButtonsVisible", at = @At("HEAD"), cancellable = true)
    private static void thaumaturge$hideFocalManipulatorSidebar(Screen screen, CallbackInfoReturnable<Boolean> cir) {
        if (screen instanceof FocalManipulatorScreen) {
            cir.setReturnValue(false);
        }
    }
}
