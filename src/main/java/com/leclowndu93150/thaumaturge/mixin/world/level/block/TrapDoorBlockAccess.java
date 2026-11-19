package com.leclowndu93150.thaumaturge.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TrapDoorBlock.class)
public interface TrapDoorBlockAccess {
    @Accessor("type")
    BlockSetType thaumaturge$trapDoorType();

    @Invoker("playSound")
    void thaumaturge$playTrapDoorSound(@Nullable Player player, Level level, BlockPos pos, boolean open);
}
