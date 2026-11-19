package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackDataPresenceMixin {
    @WrapOperation(method = "getHoverName", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;"))
    private Component thaumaturge$itemName(Item item, ItemStack stack, Operation<Component> original) {
        if (stack.getTag() != null && stack.getTag().contains(VanillaItemData.ITEM_NAME_TAG)) {
            Component name = ItemData.get(stack, VanillaItemData.ITEM_NAME);
            if (name != null) return name;
        }
        return original.call(item, stack);
    }

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
