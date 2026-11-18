package com.leclowndu93150.thaumaturge.mixin.forge;

import com.leclowndu93150.thaumaturge.content.equipment.ToolTierData;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TierSortingRegistry.class, remap = false)
abstract class TierSortingRegistryMixin {
    @Inject(method = "isCorrectTierForDrops", at = @At("HEAD"), cancellable = true, remap = false)
    private static void thaumaturge$sourceDropRestrictions(Tier tier, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (tier instanceof ToolTierData source) cir.setReturnValue(!state.is(source.getIncorrectBlocksForDrops()));
    }
}
