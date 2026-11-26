package com.leclowndu93150.thaumaturge.mixin.ae2;

import appeng.hooks.WrenchHook;
import appeng.util.InteractionUtil;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.compat.ae2.ThaumaturgeAe2;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.Map;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WrenchHook.class, remap = false)
abstract class WrenchHookMixin {
    @Redirect(method = "onPlayerUseBlock", at = @At(value = "INVOKE",
            target = "Lappeng/util/InteractionUtil;canWrenchRotate(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean thaumaturge$allowRotate(ItemStack stack, Player player, Level level,
            InteractionHand hand, BlockHitResult hit) {
        return InteractionUtil.canWrenchRotate(stack) || thaumaturge$pay(stack, player, false);
    }

    @Redirect(method = "onPlayerUseBlock", at = @At(value = "INVOKE",
            target = "Lappeng/util/InteractionUtil;canWrenchDisassemble(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean thaumaturge$allowDisassemble(ItemStack stack, Player player, Level level,
            InteractionHand hand, BlockHitResult hit) {
        return InteractionUtil.canWrenchDisassemble(stack) || thaumaturge$pay(stack, player, false);
    }

    @Inject(method = "onPlayerUseBlock", at = @At("RETURN"))
    private static void thaumaturge$charge(Player player, Level level, InteractionHand hand, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> callback) {
        if (!level.isClientSide && callback.getReturnValue().consumesAction()) {
            thaumaturge$pay(player.getItemInHand(hand), player, true);
        }
    }

    @Unique
    private static boolean thaumaturge$pay(ItemStack stack, Player player, boolean commit) {
        if (!(stack.getItem() instanceof ItemWand wand)) return false;
        var focus = wand.socketedFocus(stack);
        return focus.is(ThaumaturgeAe2.WRENCH_FOCUS.get()) && Spells.spellOf(focus) == null
                && (player.isCreative() || WandVisHelper.payCosts(
                        stack, player, Map.of(TTAspects.AER, 10, TTAspects.IGNIS, 10), commit, false));
    }
}
