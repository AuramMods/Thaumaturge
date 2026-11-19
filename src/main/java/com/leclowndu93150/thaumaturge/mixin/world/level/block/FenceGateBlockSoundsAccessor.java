package com.leclowndu93150.thaumaturge.mixin.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.FenceGateBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FenceGateBlock.class)
public interface FenceGateBlockSoundsAccessor {
    // Forge adds both fields under the same names in development and production.
    @Accessor(value = "openSound", remap = false)
    SoundEvent thaumaturge$openSound();

    @Accessor(value = "closeSound", remap = false)
    SoundEvent thaumaturge$closeSound();
}
