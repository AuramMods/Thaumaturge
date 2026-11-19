package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.content.entity.ProjectileLooting;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowItem.class)
abstract class BowLootingMixin {
    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/world/item/BowItem;customArrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;)Lnet/minecraft/world/entity/projectile/AbstractArrow;"))
    private AbstractArrow thaumaturge$rememberShotLooting(AbstractArrow arrow, @Local(argsOnly = true) ItemStack bow) {
        ProjectileLooting.remember(arrow, ProjectileLooting.levelOn(bow));
        return arrow;
    }
}
