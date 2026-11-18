package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.server.command.admin.ShowcaseMarker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityShowcaseProtectionMixin {
    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true)
    private void thaumaturge$protectShowcase(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && ShowcaseMarker.isPresent((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
