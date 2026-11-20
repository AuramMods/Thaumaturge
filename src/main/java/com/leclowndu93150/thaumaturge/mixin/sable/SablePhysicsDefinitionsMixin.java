package com.leclowndu93150.thaumaturge.mixin.sable;

import com.leclowndu93150.thaumaturge.compat.sable.SableBlockPhysics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinitionLoader", remap = false)
abstract class SablePhysicsDefinitionsMixin {
    @Inject(method = "applyAll", at = @At("HEAD"))
    private void thaumaturge$clearPreviousProperties(CallbackInfo ci) {
        SableBlockPhysics.clearProperties();
    }
}
