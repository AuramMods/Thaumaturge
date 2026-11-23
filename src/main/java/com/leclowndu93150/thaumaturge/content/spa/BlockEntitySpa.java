package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import com.leclowndu93150.thaumaturge.stage.OwnedProcessing;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public class BlockEntitySpa extends AbstractSyncedBlockEntity implements MenuProvider, OwnedProcessing {
    public static final int TANK_CAPACITY = 5000;
    private static final int WORK_INTERVAL_TICKS = 40;
    private static final int SPREAD_RADIUS = 2;
    private static final ResourceLocation MIXING_ID = TTIds.rl("spa_mixing");
    private static final ResourceLocation PLACEMENT_ID = TTIds.rl("spa_placement");
    private static final String OWNER_KEY = "Owner";

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncToClient();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            var rule = SpaRules.mixing(tank.getFluid().getFluid());
            return resource.is(TTItems.BATH_SALTS.get()) || rule != null && rule.enabled() && rule.accepts(resource);
        }

        @Override
        protected void onContentsChanged(int index) {
            setChanged();
        }
    };

    private boolean mix = true;
    private int counter;
    private @Nullable UUID owner;

    public BlockEntitySpa(BlockPos pos, BlockState state) {
        super(TTBlockEntities.SPA.get(), pos, state);
        exposeCapability(ForgeCapabilities.FLUID_HANDLER, side -> getTank());
        exposeCapability(ForgeCapabilities.ITEM_HANDLER, side -> side == Direction.UP ? null : getItems());
    }

    public FluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean getMix() {
        return mix;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public void toggleMix() {
        mix = !mix;
        setChanged();
        syncToClient();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntitySpa spa) {
        if (!(level instanceof ServerLevel server) || spa.counter++ % WORK_INTERVAL_TICKS != 0
                || server.hasNeighborSignal(pos)) {
            return;
        }
        Operation operation = spa.operation();
        if (operation == null || !StageRules.allowsOwner(server, spa.stageTarget(operation), spa.owner)) {
            return;
        }
        Block target = operation.output().getBlock();
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getBlock() == target && aboveState.getFluidState().isSource()) {
            for (int xx = -SPREAD_RADIUS; xx <= SPREAD_RADIUS; xx++) {
                for (int zz = -SPREAD_RADIUS; zz <= SPREAD_RADIUS; zz++) {
                    BlockPos p = pos.offset(xx, 1, zz);
                    if (spa.place(p, true, operation)) {
                        return;
                    }
                }
            }
        } else {
            spa.place(above, false, operation);
        }
    }

    private @Nullable Operation operation() {
        Fluid input = tank.getFluid().getFluid();
        if (mix) {
            var rule = SpaRules.mixing(input);
            ItemStack additive = items.getStackInSlot(0);
            if (rule == null || !rule.enabled() || tank.getFluidAmount() < rule.amount()
                    || additive.getCount() < rule.items() || !rule.accepts(additive)) {
                return null;
            }
            BlockState output = rule.result().isPresent() ? SpaRules.source(rule.result().get())
                    : TTBlocks.PURIFYING_FLUID.get().defaultBlockState();
            return output == null ? null : new Operation(output, rule.amount(), rule.items());
        }
        var rule = SpaRules.placement(input);
        if (!rule.enabled() || tank.getFluidAmount() < rule.amount()) {
            return null;
        }
        BlockState output = SpaRules.source(rule.result().orElse(input));
        return output == null ? null : new Operation(output, rule.amount(), 0);
    }

    private StageTarget stageTarget(Operation operation) {
        StageTarget target = StageTarget.recipe(StageTarget.SPA, mix ? MIXING_ID : PLACEMENT_ID)
                .withSubject(tank.getFluid().getFluid().builtInRegistryHolder().key());
        if (operation.items() > 0) {
            target = target.withSubject(items.getStackInSlot(0).getItem().builtInRegistryHolder().key());
        }
        return target;
    }

    private boolean place(BlockPos pos, boolean mustBeAdjacent, Operation operation) {
        if (!isValidLocation(pos, mustBeAdjacent, operation) || !level.setBlockAndUpdate(pos, operation.output())) {
            return false;
        }
        if (operation.items() > 0) items.extractItem(0, operation.items(), false);
        tank.drain(operation.amount(), IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    private boolean isValidLocation(BlockPos pos, boolean mustBeAdjacent, Operation operation) {
        Block target = operation.output().getBlock();
        Fluid fluid = operation.output().getFluidState().getType();
        if (fluid.getFluidType().isVaporizedOnPlacement(level, pos, new FluidStack(fluid, operation.amount()))) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockState below = level.getBlockState(pos.below());
        if (!below.isFaceSturdy(level, pos.below(), Direction.UP)
                || !state.canBeReplaced()
                || (state.getBlock() == target && state.getFluidState().isSource())) {
            return false;
        }
        if (!mustBeAdjacent) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (neighbor.getBlock() == target && neighbor.getFluidState().isSource()) {
                return true;
            }
        }
        return false;
    }

    private record Operation(BlockState output, int amount, int items) {}

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        writeClientData(output, registries);
        if (owner != null) output.putUUID(OWNER_KEY, owner);
    }

    @Override
    protected void writeClientData(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putBoolean("Mix", mix);
        output.put("Tank", tank.writeToNBT(new CompoundTag()));
        output.put("Items", items.serializeNBT());
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        owner = input.hasUUID(OWNER_KEY) ? input.getUUID(OWNER_KEY) : null;
        mix = (input.contains("Mix") ? input.getBoolean("Mix") : true);
        if (input.contains("Tank")) {
            tank.readFromNBT(input.getCompound("Tank"));
        }
        if (input.contains("Items")) {
            items.deserializeNBT(input.getCompound("Items"));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumaturge.spa");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuSpa(containerId, playerInventory, this);
    }
}
