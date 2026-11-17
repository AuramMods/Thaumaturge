package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.itemdata.ItemDataDefaults;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
abstract class ItemDataDefaultsMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void thaumaturge$bindDefaults(Item.Properties properties, CallbackInfo ci) {
        ItemDataDefaults.bind((Item) (Object) this, properties);
    }
}
