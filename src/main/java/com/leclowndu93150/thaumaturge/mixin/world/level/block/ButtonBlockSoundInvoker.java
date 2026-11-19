package com.leclowndu93150.thaumaturge.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ButtonBlock;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ButtonBlock.class)
public interface ButtonBlockSoundInvoker {
    @Invoker("playSound")
    void thaumaturge$playButtonSound(@Nullable Player player, LevelAccessor level, BlockPos pos, boolean powered);
}
