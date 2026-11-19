package com.leclowndu93150.thaumaturge.blockinteraction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

/** Dispatches the authorization event for a break requested by a caster or an enchantment. */
public final class BlockBreakAccess {
    private BlockBreakAccess() {}

    public static boolean mayBreak(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
        ItemStack tool = player.getMainHandItem();
        boolean toolBlocksBreak = !tool.isEmpty() && !tool.getItem().canAttackBlock(state, level, pos, player);
        boolean modeBlocksBreak = player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer());
        boolean operatorOnly = state.getBlock() instanceof GameMasterBlock && !player.canUseGameMasterBlocks();

        var event = new BlockEvent.BreakEvent(level, pos, state, player);
        event.setCanceled(toolBlocksBreak || modeBlocksBreak || operatorOnly);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            player.connection.send(new ClientboundBlockUpdatePacket(pos, state));
            return false;
        }
        return true;
    }
}
