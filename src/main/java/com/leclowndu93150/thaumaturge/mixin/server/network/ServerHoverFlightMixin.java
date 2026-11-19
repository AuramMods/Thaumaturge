package com.leclowndu93150.thaumaturge.mixin.server.network;

import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverFlight;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerHoverFlightMixin {
    @Shadow public ServerPlayer player;

    @ModifyExpressionValue(method = {"handlePlayerAbilities", "handleMovePlayer"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z",
                    opcode = Opcodes.GETFIELD))
    private boolean thaumaturge$allowHoverFlight(boolean nativePermission) {
        return HoverFlight.mayFly(player, nativePermission);
    }
}
