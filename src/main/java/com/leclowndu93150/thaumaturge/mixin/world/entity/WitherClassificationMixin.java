package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import java.util.function.Predicate;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherBoss.class)
abstract class WitherClassificationMixin {
    @Shadow @Final @Mutable
    private static Predicate<LivingEntity> LIVING_ENTITY_SELECTOR;

    @Inject(method = "<clinit>", at = @At(value = "FIELD", opcode = Opcodes.PUTSTATIC,
            target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;LIVING_ENTITY_SELECTOR:Ljava/util/function/Predicate;",
            shift = At.Shift.AFTER))
    private static void thaumaturge$filterTargets(CallbackInfo callback) {
        LIVING_ENTITY_SELECTOR = LIVING_ENTITY_SELECTOR.and(candidate -> !candidate.getType().is(TTEntityTags.WITHER_FRIENDS));
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;"),
            cancellable = true)
    private void thaumaturge$ignoreDamageFromFriends(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        var attacker = source.getEntity();
        if (attacker != null && attacker.getType().is(TTEntityTags.WITHER_FRIENDS)) {
            callback.setReturnValue(false);
        }
    }
}
