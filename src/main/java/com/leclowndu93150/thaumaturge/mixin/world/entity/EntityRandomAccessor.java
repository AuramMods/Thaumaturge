package com.leclowndu93150.thaumaturge.mixin.world.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityRandomAccessor {
    @Accessor("random")
    RandomSource thaumaturge$getRandom();
}
