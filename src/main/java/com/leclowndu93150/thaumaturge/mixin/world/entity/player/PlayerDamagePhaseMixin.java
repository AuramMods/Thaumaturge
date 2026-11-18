package com.leclowndu93150.thaumaturge.mixin.world.entity.player;

import com.leclowndu93150.thaumaturge.event.combat.DamagePhaseEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Player.class)
abstract class PlayerDamagePhaseMixin {
    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"))
    private void thaumaturge$beforeAbsorption(DamageSource source, float damage, CallbackInfo ci) {
        var player = (Player) (Object) this;
        MinecraftForge.EVENT_BUS.post(new DamagePhaseEvent.BeforeAbsorption(player, source, damage));
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE_ASSIGN", remap = false,
            target = "Lnet/minecraftforge/common/ForgeHooks;onLivingDamage(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void thaumaturge$afterAbsorbedDamage(DamageSource source, float damage, CallbackInfo ci, float healthDamage) {
        if (healthDamage == 0.0F) {
            var player = (Player) (Object) this;
            MinecraftForge.EVENT_BUS.post(new DamagePhaseEvent.AfterDamage(player, source, healthDamage));
        }
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/entity/player/Player;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void thaumaturge$afterHealthDamage(DamageSource source, float damage, CallbackInfo ci, float healthDamage) {
        var player = (Player) (Object) this;
        MinecraftForge.EVENT_BUS.post(new DamagePhaseEvent.AfterDamage(player, source, healthDamage));
    }
}
