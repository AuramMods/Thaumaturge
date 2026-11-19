package com.leclowndu93150.thaumaturge.content.equipment;

import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.event.block.BlockDropsEvent;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.state.AttachmentStorage;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

final class InfusionDropFixture {
    private static final Capability<AttachmentStorage> STATE = CapabilityManager.get(new CapabilityToken<>() {});

    private InfusionDropFixture() {}

    static BlockDropsEvent event(ServerLevel level, BlockState state, Player breaker, List<ItemEntity> drops) {
        if (breaker == null) {
            return new BlockDropsEvent(level, BlockPos.ZERO, state, null, null, ItemStack.EMPTY, drops);
        }
        var storage = new AttachmentStorage(TTAttachments.ATTACHMENTS, level::registryAccess, () -> {});
        when(breaker.getCapability(STATE)).thenReturn(LazyOptional.of(() -> storage));
        return new BlockDropsEvent(level, BlockPos.ZERO, state, null, breaker, breaker.getMainHandItem(), drops);
    }
}
