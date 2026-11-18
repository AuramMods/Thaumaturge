package com.leclowndu93150.thaumaturge.mixin.world.entity;

import com.leclowndu93150.thaumaturge.content.entity.shield.AbsorptionCapacity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({LivingEntity.class, Player.class})
abstract class AbsorptionAmountMixin {
    @ModifyVariable(method = "setAbsorptionAmount", at = @At("HEAD"), argsOnly = true)
    private float thaumaturge$limitAbsorption(float amount) {
        return AbsorptionCapacity.clamp((LivingEntity) (Object) this, amount);
    }
}
