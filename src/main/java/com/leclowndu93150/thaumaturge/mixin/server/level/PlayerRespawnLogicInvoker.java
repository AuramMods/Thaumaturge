package com.leclowndu93150.thaumaturge.mixin.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerRespawnLogic.class)
public interface PlayerRespawnLogicInvoker {
    @Invoker("getOverworldRespawnPos")
    static BlockPos thaumaturge$getOverworldRespawnPos(ServerLevel level, int x, int z) {
        throw new AssertionError();
    }
}
