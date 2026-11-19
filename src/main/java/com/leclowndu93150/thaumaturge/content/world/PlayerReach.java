package com.leclowndu93150.thaumaturge.content.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;

/** Interaction distances for menus and scans that use the 1.21 reach rules. */
public final class PlayerReach {
    private static final double CREATIVE_ENTITY_BONUS = 2.0;

    private PlayerReach() {}

    public static double entityRange(Player player) {
        double range = player.getAttributeValue(ForgeMod.ENTITY_REACH.get());
        return range + (player.isCreative() ? CREATIVE_ENTITY_BONUS : 0.0);
    }

    public static boolean canInteractWithBlock(Player player, BlockPos pos, double padding) {
        return within(player, new AABB(pos), player.getBlockReach() + padding);
    }

    public static boolean canInteractWithEntity(Player player, Entity target, double padding) {
        return !target.isRemoved() && within(player, target.getBoundingBox(), entityRange(player) + padding);
    }

    private static boolean within(Player player, AABB bounds, double range) {
        return bounds.distanceToSqr(player.getEyePosition()) < range * range;
    }
}
