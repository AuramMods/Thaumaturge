package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.content.entity.ProjectileLooting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
abstract class CrossbowLootingMixin {
    @Inject(method = "getArrow", at = @At("RETURN"))
    private static void thaumaturge$rememberShotLooting(Level level, LivingEntity shooter,
            ItemStack crossbow, ItemStack ammunition, CallbackInfoReturnable<AbstractArrow> callback) {
        ProjectileLooting.remember(callback.getReturnValue(), ProjectileLooting.levelOn(crossbow));
    }
}
