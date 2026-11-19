package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
abstract class LivingEntityClassificationMixin {
    @ModifyReturnValue(method = "canBreatheUnderwater", at = @At("RETURN"))
    private boolean thaumaturge$breathesUnderwater(boolean nativeResult) {
        var type = ((LivingEntity) (Object) this).getType();
        return nativeResult || type.is(TTEntityTags.CAN_BREATHE_UNDER_WATER);
    }

    @ModifyReturnValue(method = "isInvertedHealAndHarm", at = @At("RETURN"))
    private boolean thaumaturge$invertsHealAndHarm(boolean nativeResult) {
        var type = ((LivingEntity) (Object) this).getType();
        return nativeResult || type.is(TTEntityTags.INVERTED_HEALING_AND_HARM);
    }

    @ModifyExpressionValue(method = "canBeAffected", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getMobType()Lnet/minecraft/world/entity/MobType;"))
    private MobType thaumaturge$poisonAndRegenerationClassification(MobType nativeType) {
        var type = ((LivingEntity) (Object) this).getType();
        return type.is(TTEntityTags.IGNORES_POISON_AND_REGEN) ? MobType.UNDEAD : nativeType;
    }
}
