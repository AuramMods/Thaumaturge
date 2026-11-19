package com.leclowndu93150.thaumaturge.mixin.world.entity.player;

import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class PlayerMiningEfficiencyMixin {
    @WrapOperation(method = "getDigSpeed", remap = false, at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/world/entity/player/Inventory;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"))
    private float thaumaturge$applyEquippedMiningEfficiency(Inventory inventory, BlockState state, Operation<Float> original,
            @Share("efficiencyApplied") LocalBooleanRef efficiencyApplied) {
        float speed = original.call(inventory, state);
        if (speed > 1.0F) {
            var player = (Player) (Object) this;
            var attribute = TTAttributes.MINING_EFFICIENCY.orElse(null);
            var efficiency = attribute == null ? null : player.getAttribute(attribute);
            if (efficiency != null) {
                efficiencyApplied.set(true);
                return speed + (float) efficiency.getValue();
            }
        }
        return speed;
    }

    @WrapOperation(method = "getDigSpeed", remap = false, at = @At(value = "INVOKE", remap = true,
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getBlockEfficiency(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int thaumaturge$skipVanillaEnchantBonusWhenApplied(LivingEntity entity, Operation<Integer> original,
            @Share("efficiencyApplied") LocalBooleanRef efficiencyApplied) {
        return efficiencyApplied.get() ? 0 : original.call(entity);
    }
}
