package com.leclowndu93150.thaumaturge.mixin.client.auram;

import com.leclowndu93150.thaumaturge.compat.auram.AuramVoxelBridge;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "art.arcane.auram.vapor.client.VaporAmbient", remap = false)
abstract class AuramVaporMixin {
    @Inject(method = "sample", at = @At("RETURN"), cancellable = true, require = 0)
    private static void thaumaturge$addVapor(ClientLevel level, BlockPos camera, CallbackInfoReturnable<Object> callback) {
        Object ambient = callback.getReturnValue();
        Object combined = AuramVoxelBridge.addVapor(level, camera, ambient);
        if (combined != ambient) callback.setReturnValue(combined);
    }
}
