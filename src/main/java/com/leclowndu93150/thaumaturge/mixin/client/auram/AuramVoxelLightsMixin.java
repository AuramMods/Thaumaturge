package com.leclowndu93150.thaumaturge.mixin.client.auram;

import com.leclowndu93150.thaumaturge.compat.auram.AuramVoxelBridge;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "art.arcane.auram.tconstruct.client.TinkersFluidLights", remap = false)
abstract class AuramVoxelLightsMixin {
    @Inject(method = "collect", at = @At("TAIL"), require = 0)
    private static void thaumaturge$addLights(ClientLevel level, @Coerce Object origin, Int2IntOpenHashMap colors,
                                             CallbackInfo callback) {
        AuramVoxelBridge.addLights(level, origin, colors);
    }
}
