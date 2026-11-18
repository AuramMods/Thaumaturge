package com.leclowndu93150.thaumaturge.mixin.world.entity.projectile;

import com.leclowndu93150.thaumaturge.content.entity.CustomGravity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrowableProjectile.class)
abstract class ThrowableProjectileGravityMixin {
    @Shadow
    protected abstract float getGravity();

    @Inject(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;checkInsideBlocks()V", shift = At.Shift.AFTER))
    private void thaumaturge$applyCustomGravityBeforeMovement(CallbackInfo ci) {
        var projectile = (ThrowableProjectile) (Object) this;
        if (projectile instanceof CustomGravity custom && !projectile.isNoGravity()) {
            double gravity = custom.customGravity();
            if (gravity != 0.0) {
                projectile.setDeltaMovement(projectile.getDeltaMovement().add(0.0, -gravity, 0.0));
            }
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;getGravity()F"))
    private float thaumaturge$skipLateGravityForCustomProjectiles(ThrowableProjectile projectile) {
        return projectile instanceof CustomGravity ? 0.0F : getGravity();
    }
}
