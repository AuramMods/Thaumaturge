package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.blockinteraction.BlockBreakAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public final class SpellBlockAccess {
    private SpellBlockAccess() {}

    public static boolean mayBreak(CastContext ctx, BlockPos pos) {
        ServerLevel level = ctx.level();
        LivingEntity caster = ctx.caster();
        if (caster instanceof ServerPlayer player) {
            return player.mayInteract(level, pos)
                    && BlockBreakAccess.mayBreak(level, player, pos, level.getBlockState(pos));
        }
        return mayGrief(ctx, caster);
    }

    public static boolean mayPlace(CastContext ctx, BlockPos pos) {
        ServerLevel level = ctx.level();
        LivingEntity caster = ctx.caster();
        if (caster instanceof ServerPlayer player) {
            return player.mayInteract(level, pos)
                    && !ForgeEventFactory.onBlockPlace(
                            player, BlockSnapshot.create(level.dimension(), level, pos), Direction.UP);
        }
        return mayGrief(ctx, caster);
    }

    private static boolean mayGrief(CastContext ctx, LivingEntity caster) {
        if (caster == null && ctx.casterId().isPresent()) {
            return false;
        }
        return ForgeEventFactory.getMobGriefingEvent(ctx.level(), caster);
    }
}
