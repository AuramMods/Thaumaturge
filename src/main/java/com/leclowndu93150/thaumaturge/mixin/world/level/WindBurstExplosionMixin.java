package com.leclowndu93150.thaumaturge.mixin.world.level;

import com.leclowndu93150.thaumaturge.content.spell.action.WindBurstExplosion;
import com.leclowndu93150.thaumaturge.content.spell.action.WindBurstLanding;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
public abstract class WindBurstExplosionMixin {
    @WrapOperation(method = "explode", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;ignoreExplosion()Z"))
    private boolean thaumaturge$ignoreFlyingPlayers(Entity entity, Operation<Boolean> original) {
        if ((Object) this instanceof WindBurstExplosion
                && entity instanceof Player player && player.getAbilities().flying) {
            return true;
        }
        return original.call(entity);
    }

    @WrapOperation(method = "explode", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean thaumaturge$skipWindDamage(Entity entity, DamageSource source, float damage,
            Operation<Boolean> original) {
        return !((Object) this instanceof WindBurstExplosion) && original.call(entity, source, damage);
    }

    @WrapOperation(method = "explode", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void thaumaturge$recordLandingHeight(Entity entity, Vec3 movement, Operation<Void> original) {
        WindBurstLanding.onBlastImpulse(entity, (Object) this instanceof WindBurstExplosion);
        original.call(entity, movement);
    }
}
