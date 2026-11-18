package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
abstract class LivingEntityJumpStrengthMixin {
    @ModifyConstant(method = "getJumpPower", constant = @Constant(floatValue = 0.42F))
    private float thaumaturge$useJumpStrength(float nativePower) {
        var entity = (LivingEntity) (Object) this;
        var attribute = TTAttributes.JUMP_STRENGTH.orElse(null);
        var strength = attribute == null ? null : entity.getAttribute(attribute);
        return strength == null ? nativePower : (float) strength.getValue();
    }
}
