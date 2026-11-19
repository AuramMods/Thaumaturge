package com.leclowndu93150.thaumaturge.mixin.server.level;

import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverFlight;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerHoverFlightMixin implements HoverFlight {
    @Unique private boolean thaumaturge$hoverFlight;

    @Override
    public boolean thaumaturge$hasHoverFlight() {
        return thaumaturge$hoverFlight;
    }

    @Override
    public void thaumaturge$setHoverFlight(boolean granted) {
        thaumaturge$hoverFlight = granted;
    }

    @ModifyArg(method = "onUpdateAbilities", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/game/ClientboundPlayerAbilitiesPacket;<init>(Lnet/minecraft/world/entity/player/Abilities;)V"))
    private Abilities thaumaturge$sendHoverPermission(Abilities original) {
        if (!thaumaturge$hoverFlight || original.mayfly) return original;

        // Use a packet snapshot so the player's base flight permission remains owned by vanilla and other mods.
        var packetAbilities = new Abilities();
        packetAbilities.mayfly = true;
        packetAbilities.flying = original.flying;
        packetAbilities.invulnerable = original.invulnerable;
        packetAbilities.instabuild = original.instabuild;
        packetAbilities.mayBuild = original.mayBuild;
        packetAbilities.setFlyingSpeed(original.getFlyingSpeed());
        packetAbilities.setWalkingSpeed(original.getWalkingSpeed());
        return packetAbilities;
    }
}
