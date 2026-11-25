package com.leclowndu93150.thaumaturge.mixin.client.auram;

import com.leclowndu93150.thaumaturge.compat.auram.AuramVoxelBridge;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "art.arcane.auram.vapor.client.VaporAmbient", remap = false)
abstract class AuramVaporMixin {
    private static final String SAMPLE = "sample(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/core/BlockPos;";
    private static final String SOURCES = ")Lart/arcane/auram/vapor/client/VaporAmbient$Sources;";

    @Inject(method = SAMPLE + SOURCES, at = @At("RETURN"), cancellable = true, require = 0)
    private static void thaumaturge$addLegacyVapor(
            ClientLevel level, BlockPos camera, CallbackInfoReturnable<Object> callback) {
        thaumaturge$addVapor(level, camera, null, callback);
    }

    @Inject(method = SAMPLE + "Lart/arcane/auram/field/VoxelGrid$Position;" + SOURCES,
            at = @At("RETURN"), cancellable = true, require = 0)
    private static void thaumaturge$addWindowedVapor(
            ClientLevel level, BlockPos camera, @Coerce Object origin, CallbackInfoReturnable<Object> callback) {
        thaumaturge$addVapor(level, camera, origin, callback);
    }

    @Unique
    private static void thaumaturge$addVapor(
            ClientLevel level, BlockPos camera, @Nullable Object origin, CallbackInfoReturnable<Object> callback) {
        Object ambient = callback.getReturnValue();
        Object combined = AuramVoxelBridge.addVapor(level, camera, origin, ambient);
        if (combined != ambient) callback.setReturnValue(combined);
    }
}
