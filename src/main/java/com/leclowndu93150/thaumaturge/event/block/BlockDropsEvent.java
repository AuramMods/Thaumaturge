package com.leclowndu93150.thaumaturge.event.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jspecify.annotations.Nullable;

/**
 * Fired on the Forge game bus with the loot entities from {@code Block.dropResources}, before they spawn.
 * Cancellation discards the items and skips the post-break callback in this call.
 * Player break experience awarded by Forge's separate break event follows that event's rules.
 */
@Cancelable
public final class BlockDropsEvent extends BlockEvent {
    private final @Nullable BlockEntity blockEntity;
    private final @Nullable Entity breaker;
    private final ItemStack tool;
    private final List<ItemEntity> drops;

    public BlockDropsEvent(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Entity breaker, ItemStack tool, List<ItemEntity> drops) {
        super(level, pos, state);
        this.blockEntity = blockEntity;
        this.breaker = breaker;
        this.tool = tool;
        this.drops = drops;
    }

    @Override
    public ServerLevel getLevel() {
        return (ServerLevel) super.getLevel();
    }

    public @Nullable BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public @Nullable Entity getBreaker() {
        return breaker;
    }

    public ItemStack getTool() {
        return tool;
    }

    /** Mutable list of entities that will spawn if the event succeeds. */
    public List<ItemEntity> getDrops() {
        return drops;
    }
}
