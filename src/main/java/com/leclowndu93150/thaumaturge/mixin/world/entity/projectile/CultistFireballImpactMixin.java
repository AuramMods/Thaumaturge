package com.leclowndu93150.thaumaturge.mixin.world.entity.projectile;

import com.leclowndu93150.thaumaturge.content.entity.cult.CultistFireballs;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmallFireball.class)
abstract class CultistFireballImpactMixin {
    @Inject(method = "onHitBlock", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$checkCultistGriefing(BlockHitResult hit, CallbackInfo callback) {
        if (CultistFireballs.blocksImpact((SmallFireball) (Object) this)) {
            callback.cancel();
        }
    }
}
