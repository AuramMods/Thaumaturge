package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.implementations.blockentities.ICrankable;
import appeng.capabilities.Capabilities;
import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A bare hand, golem Use seal, or AE2 crank can drive adjacent AE2 machines. */
final class CrankGearbox extends Block implements EntityBlock {
    private final boolean thaumium;

    CrankGearbox(Properties properties, boolean thaumium) {
        super(properties);
        this.thaumium = thaumium;
    }

    @Override
    public GearboxEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GearboxEntity(pos, state, thaumium);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder loot) {
        return List.of(new ItemStack(this));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof GearboxEntity box && box.canTurn()) {
            box.applyTurn();
            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.25F, 1.1F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    static final class GearboxEntity extends RegistryBlockEntity implements ICrankable {
        private static final Direction[] OUTPUTS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN
        };
        private final boolean thaumium;
        private int nextOutput;
        private long lastTurn = Long.MIN_VALUE;

        GearboxEntity(BlockPos pos, BlockState state, boolean thaumium) {
            super((thaumium ? ThaumaturgeAe2.THAUMIUM_GEARBOX_ENTITY : ThaumaturgeAe2.IRON_GEARBOX_ENTITY).get(),
                    pos, state);
            this.thaumium = thaumium;
            exposeCapability(Capabilities.CRANKABLE, side -> side == Direction.UP ? this : null);
        }

        @Override
        public boolean canTurn() {
            if (!ready()) return false;
            for (Direction side : OUTPUTS) {
                var target = target(side);
                if (target != null && target.canTurn()) return true;
            }
            return false;
        }

        @Override
        public void applyTurn() {
            if (!ready()) return;
            int turnBudget = thaumium ? OUTPUTS.length : 1;
            for (int step = 0; step < OUTPUTS.length && turnBudget > 0; step++) {
                int index = (nextOutput + step) % OUTPUTS.length;
                var target = target(OUTPUTS[index]);
                if (target != null && target.canTurn()) {
                    target.applyTurn();
                    lastTurn = level.getGameTime();
                    turnBudget--;
                    if (!thaumium) {
                        nextOutput = (index + 1) % OUTPUTS.length;
                        setChanged();
                    }
                }
            }
        }

        private boolean ready() {
            return level != null && !level.isClientSide
                    && (lastTurn == Long.MIN_VALUE || level.getGameTime() - lastTurn >= (thaumium ? 8 : 12));
        }

        private @Nullable ICrankable target(Direction side) {
            var target = ICrankable.get(level, worldPosition.relative(side), side.getOpposite());
            return target instanceof GearboxEntity ? null : target;
        }

        @Override
        protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
            super.loadAdditional(input, registries);
            nextOutput = Math.floorMod(input.getInt("next_output"), OUTPUTS.length);
        }

        @Override
        protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
            super.saveAdditional(output, registries);
            output.putInt("next_output", nextOutput);
        }
    }
}
