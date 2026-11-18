package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.datamap.builtin.BuiltinDataMaps;
import java.util.Optional;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
abstract class AxeDataMapMixin {
    @Inject(method = "getAxeStrippingState", at = @At("HEAD"), cancellable = true, remap = false)
    private static void thaumaturge$mappedStrippingState(BlockState original, CallbackInfoReturnable<BlockState> cir) {
        BlockState stripped = BuiltinDataMaps.strippedState(original);
        if (stripped != null) cir.setReturnValue(stripped);
    }
    @Inject(method = "getStripped", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$mappedVanillaStrippingState(BlockState original, CallbackInfoReturnable<Optional<BlockState>> cir) {
        BlockState stripped = BuiltinDataMaps.strippedState(original);
        if (stripped != null) cir.setReturnValue(Optional.of(stripped));
    }
}
