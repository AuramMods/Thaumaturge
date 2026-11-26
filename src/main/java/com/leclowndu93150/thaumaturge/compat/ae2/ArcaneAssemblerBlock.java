package com.leclowndu93150.thaumaturge.compat.ae2;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jspecify.annotations.Nullable;

final class ArcaneAssemblerBlock extends Block implements EntityBlock {
    ArcaneAssemblerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ArcaneAssemblerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneAssemblerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel) || type != ThaumaturgeAe2.ARCANE_ASSEMBLER_ENTITY.get()) return null;
        return (world, pos, currentState, entity) -> ((ArcaneAssemblerBlockEntity) entity).flushOutput();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder loot) {
        return List.of(new ItemStack(this));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof ArcaneAssemblerBlockEntity be) {
            be.setOwner(player);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
        if (state.getBlock() != replacement.getBlock() && level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof ArcaneAssemblerBlockEntity assembler) {
            assembler.removeOutput().forEach(stack -> Block.popResource(level, pos, stack));
        }
        super.onRemove(state, level, pos, replacement, moved);
    }
}
