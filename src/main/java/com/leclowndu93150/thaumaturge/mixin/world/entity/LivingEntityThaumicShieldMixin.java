package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.equipment.shield.ShieldEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
abstract class LivingEntityThaumicShieldMixin {
    private static final float WARD_DURABILITY_MULTIPLIER = 2.0F;

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean thaumaturge$wardDamage(LivingEntity blocker, DamageSource source, Operation<Boolean> original,
            @Share("warded") LocalBooleanRef warded) {
        if (original.call(blocker, source)) return true;

        boolean canWard = ShieldEvents.wards(blocker, source);
        warded.set(canWard);
        return canWard;
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraftforge/common/ForgeHooks;onShieldBlock(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Lnet/minecraftforge/event/entity/living/ShieldBlockEvent;"))
    private ShieldBlockEvent thaumaturge$sapAfterBlock(LivingEntity blocker, DamageSource source, float amount,
            Operation<ShieldBlockEvent> original) {
        var event = original.call(blocker, source, amount);
        if (!event.isCanceled()) ShieldEvents.sapAttacker(blocker, source);
        return event;
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"))
    private void thaumaturge$chargeWardDurability(LivingEntity blocker, float amount, Operation<Void> original,
            @Local ShieldBlockEvent event, @Share("warded") LocalBooleanRef warded) {
        float durability = warded.get() ? event.getBlockedDamage() * WARD_DURABILITY_MULTIPLIER : amount;
        if (durability > 0.0F) original.call(blocker, durability);
    }
}
