package com.leclowndu93150.thaumaturge.mixin.world.level.block.entity;

import com.leclowndu93150.thaumaturge.itemdata.BlockItemDataBridge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
abstract class BlockEntityItemDataMixin {
    @Inject(method = "saveToItem", at = @At("RETURN"))
    private void thaumaturge$saveItemData(ItemStack stack, CallbackInfo ci) {
        BlockItemDataBridge.saveToItem((BlockEntity) (Object) this, stack);
    }
}
