package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.features.IPlayerRegistry;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jspecify.annotations.Nullable;

final class EssentiaProviderBlock extends Block implements EntityBlock {
    private final boolean infusion;

    EssentiaProviderBlock(Properties properties, boolean infusion) {
        super(properties);
        this.infusion = infusion;
    }

    @Override
    public EssentiaProviderBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaProviderBlockEntity(pos, state, infusion);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder loot) {
        return List.of(new ItemStack(this));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof EssentiaProviderBlockEntity be) {
            be.setOwner(IPlayerRegistry.getPlayerId(player));
        }
    }
}
