package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public record PendingSwap(
        BlockPos pos,
        BlockState originalState,
        Optional<ItemStack> resultStack,
        Optional<BlockState> resultState,
        int ticksRemaining,
        Optional<StageAuthorization> authority) {
    public static final Codec<PendingSwap> CODEC = RecordCodecBuilder.create(i -> i.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(PendingSwap::pos),
                    BlockState.CODEC.fieldOf("original_state").forGetter(PendingSwap::originalState),
                    Codecs.optionalFieldOf(ItemStackCodecs.CODEC, "result_stack").forGetter(PendingSwap::resultStack),
                    Codecs.optionalFieldOf(BlockState.CODEC, "result_state").forGetter(PendingSwap::resultState),
                    Codec.INT.fieldOf("ticks_remaining").forGetter(PendingSwap::ticksRemaining),
                    Codecs.optionalFieldOf(StageAuthorization.CODEC, "authority").forGetter(PendingSwap::authority))
            .apply(i, PendingSwap::new));

    public PendingSwap withTicks(int ticks) {
        return new PendingSwap(pos, originalState, resultStack, resultState, ticks, authority);
    }

    public PendingSwap withAuthority(@Nullable StageAuthorization authorization) {
        return new PendingSwap(pos, originalState, resultStack, resultState, ticksRemaining,
                Optional.ofNullable(authorization));
    }

    public static PendingSwap dropItem(BlockPos pos, BlockState original, ItemStack stack, int delay) {
        return new PendingSwap(pos.immutable(), original, Optional.of(stack.copy()), Optional.empty(), delay,
                Optional.empty());
    }

    public static PendingSwap placeBlock(BlockPos pos, BlockState original, BlockState placed, int delay) {
        return new PendingSwap(pos.immutable(), original, Optional.empty(), Optional.of(placed), delay,
                Optional.empty());
    }

    public static PendingSwap clearOnly(BlockPos pos, BlockState original, int delay) {
        return new PendingSwap(pos.immutable(), original, Optional.empty(), Optional.empty(), delay, Optional.empty());
    }

    public @Nullable ItemStack stackResult() {
        return this.resultStack.orElse(null);
    }

    public @Nullable BlockState stateResult() {
        return this.resultState.orElse(null);
    }
}
