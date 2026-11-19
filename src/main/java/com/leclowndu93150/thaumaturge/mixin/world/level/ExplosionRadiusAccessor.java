package com.leclowndu93150.thaumaturge.mixin.world.level;

import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionRadiusAccessor {
    @Accessor("radius")
    float thaumaturge$getRadius();
}
