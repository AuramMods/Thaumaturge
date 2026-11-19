package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.event.block.BlockDropsEvent;
import com.leclowndu93150.thaumaturge.testing.AttachmentFixture;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

final class InfusionDropFixture {
    private InfusionDropFixture() {}

    static BlockDropsEvent event(ServerLevel level, BlockState state, Player breaker, List<ItemEntity> drops) {
        if (breaker == null) {
            return new BlockDropsEvent(level, BlockPos.ZERO, state, null, null, ItemStack.EMPTY, drops);
        }
        AttachmentFixture.attach(breaker, level::registryAccess);
        return new BlockDropsEvent(level, BlockPos.ZERO, state, null, breaker, breaker.getMainHandItem(), drops);
    }
}
