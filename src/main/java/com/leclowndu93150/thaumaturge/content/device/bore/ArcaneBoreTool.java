package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

public final class ArcaneBoreTool {
    private static final int MIN_RADIUS = 2;
    private static final int ENCHANTABILITY_PER_RADIUS = 3;
    private static final int RADIUS_PER_DESTRUCTIVE = 2;
    private static final int DEPTH_PER_RADIUS = 8;
    private static final int DEPTH_PER_BURROWING = 16;
    private static final int DESTROY_SPEED_DIVISOR = 2;

    private ArcaneBoreTool() {}

    public static boolean isPickaxe(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ItemTags.PICKAXES) || stack.canPerformAction(ToolActions.PICKAXE_DIG));
    }

    public static boolean valid(ItemStack stack) {
        if (!isPickaxe(stack)) {
            return false;
        }
        return !stack.isDamageableItem() || stack.getDamageValue() + 1 < stack.getMaxDamage();
    }

    public static int digRadius(ItemStack stack) {
        int radius = 0;
        if (isPickaxe(stack)) {
            radius = stack.getEnchantmentValue() / ENCHANTABILITY_PER_RADIUS;
            radius += InfusionEnchantmentHelper.level(stack, InfusionEnchantment.DESTRUCTIVE) * RADIUS_PER_DESTRUCTIVE;
        }
        return radius <= 1 ? MIN_RADIUS : radius;
    }

    public static int digDepth(ItemStack stack) {
        return digRadius(stack) * DEPTH_PER_RADIUS
                + InfusionEnchantmentHelper.level(stack, InfusionEnchantment.BURROWING) * DEPTH_PER_BURROWING;
    }

    public static int fortune(Level level, ItemStack stack) {
        if (!valid(stack)) {
            return 0;
        }
        int fortune = stack.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        return Math.max(fortune, InfusionEnchantmentHelper.level(stack, InfusionEnchantment.SOUNDING));
    }

    public static int digSpeed(Level level, ItemStack stack, BlockState state) {
        if (!valid(stack)) {
            return 0;
        }
        int speed = (int) (stack.getDestroySpeed(state) / DESTROY_SPEED_DIVISOR);
        speed += stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
        return speed;
    }

    public static int refining(ItemStack stack) {
        return InfusionEnchantmentHelper.level(stack, InfusionEnchantment.REFINING);
    }

    public static boolean silkTouch(Level level, ItemStack stack) {
        return !stack.isEmpty() && stack.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0;
    }

    static boolean damageStationaryTool(ItemStack tool, RandomSource random) {
        if (tool.hurt(1, random, null)) {
            tool.shrink(1);
            return true;
        }
        return false;
    }
}
