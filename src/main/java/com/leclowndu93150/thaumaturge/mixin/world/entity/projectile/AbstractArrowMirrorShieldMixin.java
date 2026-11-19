package com.leclowndu93150.thaumaturge.mixin.world.entity.projectile;

import com.leclowndu93150.thaumaturge.content.equipment.shield.MirrorShieldArrow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
abstract class AbstractArrowMirrorShieldMixin implements MirrorShieldArrow {
    @Unique private @Nullable Integer thaumaturge$mirrorBlockerId;

    @Override
    public void thaumaturge$markMirrorReflection(int blockerId) {
        thaumaturge$mirrorBlockerId = blockerId;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/minecraftforge/event/ForgeEventFactory;onProjectileImpactResultNullable(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/world/phys/HitResult;)Lnet/minecraftforge/event/entity/ProjectileImpactEvent$ImpactResult;"))
    private void thaumaturge$beginImpact(CallbackInfo ci) {
        thaumaturge$mirrorBlockerId = null;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", remap = false,
            target = "Lit/unimi/dsi/fastutil/ints/IntOpenHashSet;add(I)Z"))
    private boolean thaumaturge$skipOnlyTheReflectedImpact(IntOpenHashSet ignored, int target, Operation<Boolean> original) {
        // Forge normally remembers SKIP_ENTITY for the arrow's lifetime. A reflection only skips this impact.
        var blockerId = thaumaturge$mirrorBlockerId;
        thaumaturge$mirrorBlockerId = null;
        return (blockerId == null || blockerId != target) && original.call(ignored, target);
    }
}
