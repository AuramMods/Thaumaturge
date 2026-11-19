package com.leclowndu93150.thaumaturge.mixin.world.entity.monster;

import com.leclowndu93150.thaumaturge.content.entity.ProjectileLooting;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({AbstractSkeleton.class, Illusioner.class})
abstract class MobBowLootingMixin {
    @ModifyExpressionValue(method = "performRangedAttack", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/InteractionHand;"))
    private InteractionHand thaumaturge$readBowLooting(
            InteractionHand hand, @Share("shotLooting") LocalIntRef looting) {
        looting.set(ProjectileLooting.levelOn(((LivingEntity) (Object) this).getItemInHand(hand)));
        return hand;
    }

    @WrapOperation(method = "performRangedAttack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shoot(DDDFF)V"))
    private void thaumaturge$rememberShotLooting(AbstractArrow arrow, double x, double y, double z,
            float speed, float inaccuracy, Operation<Void> original, @Share("shotLooting") LocalIntRef looting) {
        ProjectileLooting.remember(arrow, looting.get());
        original.call(arrow, x, y, z, speed, inaccuracy);
    }
}
