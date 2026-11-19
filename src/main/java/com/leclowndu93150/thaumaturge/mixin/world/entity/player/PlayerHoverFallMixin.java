package com.leclowndu93150.thaumaturge.mixin.world.entity.player;

import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverFlight;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class PlayerHoverFallMixin {
    @ModifyExpressionValue(method = "causeFallDamage", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z", opcode = Opcodes.GETFIELD))
    private boolean thaumaturge$useFlightFallHandling(boolean nativePermission) {
        return HoverFlight.mayFly((Player) (Object) this, nativePermission);
    }
}
