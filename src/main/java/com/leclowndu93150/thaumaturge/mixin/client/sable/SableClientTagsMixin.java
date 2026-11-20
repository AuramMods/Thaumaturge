package com.leclowndu93150.thaumaturge.mixin.client.sable;

import com.leclowndu93150.thaumaturge.compat.sable.SableBlockPhysics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class SableClientTagsMixin {
    @Inject(method = "handleUpdateTags", at = @At("RETURN"))
    private void thaumaturge$clearPreviousProperties(ClientboundUpdateTagsPacket packet, CallbackInfo ci) {
        var connection = ((ClientPacketListener) (Object) this).getConnection();
        if (!connection.isMemoryConnection()) {
            // The server applies the same properties to the shared states in singleplayer.
            SableBlockPhysics.clearProperties();
        }
    }
}
