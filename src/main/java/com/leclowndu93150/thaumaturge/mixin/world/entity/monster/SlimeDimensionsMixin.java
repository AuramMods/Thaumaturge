package com.leclowndu93150.thaumaturge.mixin.world.entity.monster;

import com.leclowndu93150.thaumaturge.content.entity.EntityGeometry;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Slime.class)
abstract class SlimeDimensionsMixin {
    @ModifyConstant(method = "getDimensions", constant = @Constant(floatValue = 0.255F))
    private float thaumaturge$useAuthoredBaseSize(float nativeFactor) {
        var slime = (Slime) (Object) this;
        return EntityGeometry.of(slime.getType()) == null ? nativeFactor : 1.0F;
    }
}
