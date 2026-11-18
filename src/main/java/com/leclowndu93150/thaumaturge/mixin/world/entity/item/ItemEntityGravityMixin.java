package com.leclowndu93150.thaumaturge.mixin.world.entity.item;

import com.leclowndu93150.thaumaturge.content.entity.CustomGravity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEntity.class)
abstract class ItemEntityGravityMixin {
    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.04D))
    private double thaumaturge$adjustGravity(double original) {
        return (Object) this instanceof CustomGravity custom ? -custom.customGravity() : original;
    }
}
