package com.leclowndu93150.thaumaturge.content.device.patterncrafter;

import com.leclowndu93150.thaumaturge.stage.OwnedProcessing;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.ItemMatch;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.thaumaturge.recipe.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import com.leclowndu93150.thaumaturge.recipe.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public final class BlockEntityPatternCrafter extends AbstractSyncedBlockEntity implements OwnedProcessing {
    public static final int PATTERN_COUNT = 10;
    private static final float VIS_DRAIN = 5.0F;
    private static final String OWNER_KEY = "Owner";
    private static final int SPIN_EVENT = 1;
    private static final int SPIN_TICKS = 10;
    private static final int[] PATTERN_INPUT_COUNTS = {9, 1, 2, 2, 4, 3, 3, 6, 6, 8};
    private static final int[][] PATTERN_SLOTS = {
        {0, 1, 2, 3, 4, 5, 6, 7, 8},
        {0},
        {0, 1},
        {0, 3},
        {0, 1, 3, 4},
        {0, 1, 2},
        {0, 3, 6},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 3, 4, 6, 7},
        {0, 1, 2, 3, 5, 6, 7, 8}
    };

    private byte patternType;
    private float power;
    private int counter;
    private @Nullable UUID owner;

    public float rot;
    public float rotSpeed;
    public int rotTicks;

    public BlockEntityPatternCrafter(BlockPos pos, BlockState state) {
        super(TTBlockEntities.PATTERN_CRAFTER.get(), pos, state);
    }

    public byte patternType() {
        return patternType;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public void cycle() {
        patternType++;
        if (patternType >= PATTERN_COUNT) {
            patternType = 0;
        }
        setChanged();
        syncToClient();
    }

    void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            if (rotTicks > 0) {
                rotTicks--;
                if (rotTicks % Math.floor(Math.max(1.0F, rotSpeed)) == 0.0) {
                    level.playLocalSound(
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            TTSounds.CLACK.get(),
                            SoundSource.BLOCKS,
                            0.2F,
                            1.7F,
                            false);
                }
                rotSpeed++;
            } else {
                rotSpeed *= 0.8F;
            }
            rot += rotSpeed;
            return;
        }
        var rules = PatternCrafterRules.of(state.getBlock());
        if (!rules.enabled() || counter++ % rules.interval() != 0 || !state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        ServerLevel server = (ServerLevel) level;
        boolean legacyCharging = rules == PatternCrafterRules.DEFAULT
                && !StageRules.of(server).hasRules(StageTarget.PATTERN_CRAFTER);
        if (legacyCharging && power <= 0.0F) {
            power += AuraHelper.drainVis(server, pos, VIS_DRAIN, false);
        }
        int inputCount = PATTERN_INPUT_COUNTS[patternType];
        IItemHandler above = InvHelper.handlerAt(server, pos.above(), Direction.DOWN);
        IItemHandler below = InvHelper.handlerAt(server, pos.below(), Direction.UP);
        if (above == null || below == null) {
            return;
        }
        for (int slot = 0; slot < above.getSlots(); slot++) {
            ItemStack resource = above.getStackInSlot(slot);
            if (resource.isEmpty()) continue;
            ItemStack testStack = resource.copyWithCount(inputCount);
            ItemStack removed = InvHelper.extract(above, testStack, ItemMatch.EXACT, true);
            if (removed.getCount() != inputCount) continue;
            CraftResult result = craft(server, testStack);
            if (result == null) continue;
            if (!outputsFit(below, result.output(), result.remainders())) continue;
            if (!legacyCharging && power < rules.vis()) {
                power += AuraHelper.drainVis(server, pos, Math.min(VIS_DRAIN, rules.vis() - power), false);
                setChanged();
            }
            if (power < rules.vis()) return;

            ItemStack taken = InvHelper.extract(above, testStack, ItemMatch.EXACT, false);
            if (taken.getCount() != inputCount) {
                InvHelper.eject(server, pos, Direction.UP, taken);
                continue;
            }
            InvHelper.eject(server, pos, Direction.DOWN, result.output());
            for (ItemStack remainder : result.remainders()) {
                InvHelper.eject(server, pos, Direction.DOWN, remainder);
            }
            server.blockEvent(pos, state.getBlock(), SPIN_EVENT, 0);
            power -= rules.vis();
            setChanged();
            break;
        }
    }

    static boolean outputsFit(IItemHandler receiver, ItemStack output, List<ItemStack> remainders) {
        if (remainders.isEmpty()) {
            return InvHelper.insertStack(receiver, output, true).isEmpty();
        }
        ItemStack[] reserved = new ItemStack[receiver.getSlots()];
        if (!reserveOutput(receiver, reserved, output)) return false;
        for (ItemStack remainder : remainders) {
            if (!reserveOutput(receiver, reserved, remainder)) return false;
        }
        return true;
    }

    private static boolean reserveOutput(IItemHandler receiver, ItemStack[] reserved, ItemStack offered) {
        int remaining = offered.getCount();
        if (offered.isStackable()) {
            remaining = reserveSlots(receiver, reserved, offered, remaining, false);
        }
        return reserveSlots(receiver, reserved, offered, remaining, true) == 0;
    }

    private static int reserveSlots(
            IItemHandler receiver, ItemStack[] reserved, ItemStack offered, int remaining, boolean emptySlots) {
        for (int slot = 0; slot < reserved.length && remaining > 0; slot++) {
            ItemStack reservation = reserved[slot];
            ItemStack occupied = reservation == null ? receiver.getStackInSlot(slot) : reservation;
            if (emptySlots ? !occupied.isEmpty() : !ItemHandlerHelper.canItemStacksStackRelaxed(occupied, offered)) {
                continue;
            }
            // The receiver sees its original contents during simulation, so include and subtract earlier reservations.
            int prior = reservation == null ? 0 : reservation.getCount();
            int attempt = (int) Math.min(Integer.MAX_VALUE, (long) prior + remaining);
            ItemStack rejected = receiver.insertItem(slot, offered.copyWithCount(attempt), true);
            int accepted = attempt - rejected.getCount() - prior;
            if (accepted > 0) {
                reserved[slot] = offered.copyWithCount(prior + accepted);
                remaining -= accepted;
            }
        }
        return remaining;
    }

    private CraftResult craft(ServerLevel server, ItemStack input) {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(ItemStack.EMPTY);
        }
        for (int slot : PATTERN_SLOTS[patternType]) {
            grid.set(slot, input.copyWithCount(1));
        }
        CraftingInput craftingInput = CraftingInput.of(3, 3, grid);
        Optional<RecipeHolder<CraftingRecipe>> match =
                RecipeAccess.of(server.getRecipeManager()).getRecipeFor(RecipeType.CRAFTING, craftingInput, server);
        if (match.isEmpty() || !StageRules.allowsOwner(server,
                StageTarget.recipe(StageTarget.PATTERN_CRAFTER, match.get().id())
                        .withSubject(input.getItem().builtInRegistryHolder().key()), owner)) {
            return null;
        }
        CraftingRecipe recipe = match.get().value();
        ItemStack output = recipe.assemble(craftingInput, server.registryAccess());
        if (output.isEmpty()) {
            return null;
        }
        List<ItemStack> remainders = new ArrayList<>();
        for (ItemStack remainder : recipe.getRemainingItems(craftingInput)) {
            if (!remainder.isEmpty()) {
                remainders.add(remainder);
            }
        }
        return new CraftResult(output, remainders);
    }

    @Override
    public boolean triggerEvent(int event, int param) {
        if (event == SPIN_EVENT) {
            if (level != null && level.isClientSide()) {
                rotTicks = SPIN_TICKS;
            }
            return true;
        }
        return super.triggerEvent(event, param);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        writeClientData(output, registries);
        if (owner != null) output.putUUID(OWNER_KEY, owner);
    }

    @Override
    protected void writeClientData(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putByte("Type", patternType);
        output.putFloat("Power", power);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        patternType = input.getByte("Type");
        power = input.getFloat("Power");
        owner = input.hasUUID(OWNER_KEY) ? input.getUUID(OWNER_KEY) : null;
    }

    private record CraftResult(ItemStack output, List<ItemStack> remainders) {}
}
