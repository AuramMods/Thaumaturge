package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.itemdata.BlockItemDataBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
abstract class BlockItemDataMixin {
    @Inject(method = "updateCustomBlockEntityTag(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("RETURN"))
    private void thaumaturge$applyItemData(BlockPos pos, Level level, Player player, ItemStack stack,
            BlockState state, CallbackInfoReturnable<Boolean> cir) {
        // Native placement calls this before Block.setPlacedBy and stack shrink.
        BlockItemDataBridge.applyPlacement(level, pos, stack);
    }
}
