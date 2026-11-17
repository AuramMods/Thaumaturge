package com.leclowndu93150.thaumaturge.mixin.world.level.chunk;

import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
abstract class LevelChunkRegistryContextMixin {
    @Redirect(method = "promotePendingBlockEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadStatic(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity thaumaturge$loadWithRegistries(BlockPos pos, BlockState state, CompoundTag tag) {
        var level = ((LevelChunk) (Object) this).getLevel();
        return RegistryContext.withRegistries(level.registryAccess(), () -> BlockEntity.loadStatic(pos, state, tag));
    }
}
