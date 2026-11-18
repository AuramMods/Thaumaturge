package com.leclowndu93150.thaumaturge.mixin.forge;

import com.leclowndu93150.thaumaturge.datamap.builtin.BuiltinDataMaps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeHooks.class, remap = false)
abstract class ForgeFuelDataMapMixin {
    @Redirect(method = "getBurnTime", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getBurnTime(Lnet/minecraft/world/item/crafting/RecipeType;)I"))
    private static int thaumaturge$resolveDataMapFuel(ItemStack stack, RecipeType<?> type) {
        return BuiltinDataMaps.declaredBurnTime(stack, type);
    }
}
