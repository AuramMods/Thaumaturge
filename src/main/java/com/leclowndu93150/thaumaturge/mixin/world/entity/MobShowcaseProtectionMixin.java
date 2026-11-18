package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.server.command.admin.ShowcaseMarker;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
abstract class MobShowcaseProtectionMixin {
    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$keepShowcase(CallbackInfo ci) {
        if (ShowcaseMarker.isPresent((Mob) (Object) this)) {
            ci.cancel();
        }
    }
}
