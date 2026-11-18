package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackDataPresenceMixin {
    @Redirect(method = {"<init>(Lnet/minecraft/nbt/CompoundTag;)V",
            "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V", "setTag"},
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;isDamageable(Lnet/minecraft/world/item/ItemStack;)Z", remap = false))
    private boolean thaumaturge$preserveRemovedDamage(Item item, ItemStack stack) {
        return !ItemData.isRemoved(stack, "minecraft:damage") && item.isDamageable(stack);
    }

    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$removedDamageIsNotDamageable(CallbackInfoReturnable<Boolean> cir) {
        if (ItemData.isRemoved((ItemStack) (Object) this, "minecraft:damage")) cir.setReturnValue(false);
    }
}
