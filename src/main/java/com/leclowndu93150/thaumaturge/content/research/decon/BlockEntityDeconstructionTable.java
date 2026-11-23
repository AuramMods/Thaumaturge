package com.leclowndu93150.thaumaturge.content.research.decon;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.leclowndu93150.thaumaturge.stage.OwnedProcessing;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityDeconstructionTable extends AbstractSyncedBlockEntity implements MenuProvider, OwnedProcessing {
    public static final int SLOT_INPUT = 0;
    public static final int BREAK_TIME_TICKS = 40;
    private static final int YIELD_ROLL = 80;

    private static final Component TITLE = Component.translatable("gui.thaumaturge.deconstruction_table.title");

    private final TableInventory inventory = new TableInventory();
    private @Nullable ResourceLocation resultAspect;
    private @Nullable StageTarget resultTarget;
    private int resultPoints = 1;
    private int breakTime;
    private @Nullable UUID owner;

    public BlockEntityDeconstructionTable(BlockPos pos, BlockState state) {
        super(TTBlockEntities.DECONSTRUCTION_TABLE.get(), pos, state);
    }

    public ItemStackHandler items() {
        return inventory;
    }

    public @Nullable ResourceLocation resultAspect() {
        return resultAspect;
    }

    public int breakTime() {
        return breakTime;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuDeconstructionTable(containerId, playerInventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDeconstructionTable table) {
        if (!(level instanceof ServerLevel server)) return;
        ItemStack input = table.inventory.getStackInSlot(SLOT_INPUT);
        DeconstructionRules rule = DeconstructionRules.of(input);
        if (table.resultAspect != null || !canBreak(input, rule)) {
            if (table.breakTime != BREAK_TIME_TICKS) {
                table.breakTime = BREAK_TIME_TICKS;
                table.setChanged();
            }
            return;
        }
        var item = input.getItem().builtInRegistryHolder().key();
        var target = StageTarget.recipe(StageTarget.DECONSTRUCTION_TABLE, item.location()).withSubject(item);
        if (rule.result().isPresent()) {
            target = target.withSubject(rule.result().orElseThrow().unwrapKey().orElseThrow());
        }
        if (!StageRules.allowsOwner(server, target, table.owner)) return;
        if (--table.breakTime <= 0) {
            table.breakTime = BREAK_TIME_TICKS;
            table.breakItem(server, pos, input.copy(), rule, target);
        }
        table.setChanged();
    }

    private static boolean canBreak(ItemStack input, DeconstructionRules rule) {
        return !input.isEmpty() && rule.enabled()
                && (rule.result().isPresent() || !AspectIndexAccess.index().of(input).isEmpty());
    }

    private void breakItem(ServerLevel level, BlockPos pos, ItemStack input, DeconstructionRules rule, StageTarget target) {
        Holder<IAspect> result = selectAspect(level, input, rule);
        if (result != null) {
            var key = result.unwrapKey().orElseThrow();
            if (rule.result().isEmpty()) target = target.withSubject(key);
            if (!StageRules.allowsOwner(level, target, owner)) return;
            resultAspect = key.location();
            resultTarget = target;
            resultPoints = rule.points();
        }
        input.shrink(1);
        inventory.setStackInSlot(SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
        level.playSound(
                null,
                pos,
                SoundEvents.WOOD_HIT,
                SoundSource.BLOCKS,
                0.3F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
        setChanged();
        syncToClient();
    }

    private static @Nullable Holder<IAspect> selectAspect(ServerLevel level, ItemStack input, DeconstructionRules rule) {
        var random = level.getRandom();
        if (rule.result().isPresent()) {
            return rule.chance().isEmpty() || random.nextFloat() < rule.chance().get() ? rule.result().get() : null;
        }
        AspectList primals = AspectPrimals.reduce(AspectIndexAccess.index().of(input.copyWithCount(1)));
        if (primals.isEmpty()) return null;
        boolean success = rule.chance().map(chance -> random.nextFloat() < chance)
                .orElseGet(() -> random.nextInt(YIELD_ROLL) < primals.totalAmount());
        if (!success) return null;
        List<Holder<IAspect>> choices = List.copyOf(primals.aspects());
        return choices.get(random.nextInt(choices.size()));
    }

    public void collect(ServerPlayer player) {
        if (resultAspect == null || !(getLevel() instanceof ServerLevel server) || player.level() != server) {
            return;
        }
        HolderLookup.RegistryLookup<IAspect> lookup = player.level().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        ResourceLocation collected = resultAspect;
        lookup.get(ResourceKey.create(IAspect.REGISTRY_KEY, collected)).ifPresent(holder -> {
            StageTarget target = resultTarget != null ? resultTarget
                    : StageTarget.family(StageTarget.DECONSTRUCTION_TABLE).withSubject(holder.key());
            if (!StageRules.check(server, target, player)) return;
            AspectPools.grant(player, holder, resultPoints);
            resultAspect = null;
            resultTarget = null;
            resultPoints = 1;
            setChanged();
            syncToClient();
        });
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(1);
        ItemStack stack = inventory.getStackInSlot(SLOT_INPUT);
        if (!stack.isEmpty()) {
            container.setItem(0, stack.copy());
        }
        Containers.dropContents(level, pos, container);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        inventory.deserializeNBT(input.getCompound("inventory"));
        resultAspect = TTNbt.read(input, "result_aspect", LegacyIds.IDENTIFIER_CODEC, registries)
                .orElse(null);
        resultTarget = TTNbt.read(input, "result_target", StageTarget.CODEC, registries).orElse(null);
        resultPoints = input.contains("result_points") ? Math.max(1, input.getInt("result_points")) : 1;
        owner = input.hasUUID("Owner") ? input.getUUID("Owner") : null;
        breakTime = input.contains("break_time") ? input.getInt("break_time") : BREAK_TIME_TICKS;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        writeClientData(output, registries);
        if (owner != null) output.putUUID("Owner", owner);
        if (resultTarget != null) TTNbt.store(output, "result_target", StageTarget.CODEC, registries, resultTarget);
        if (resultPoints != 1) output.putInt("result_points", resultPoints);
    }

    @Override
    protected void writeClientData(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put("inventory", inventory.serializeNBT());
        if (resultAspect != null) {
            TTNbt.store(output, "result_aspect", ResourceLocation.CODEC, registries, resultAspect);
        }
        output.putInt("break_time", breakTime);
    }

    private final class TableInventory extends ItemStackHandler {
        TableInventory() {
            super(NonNullList.withSize(1, ItemStack.EMPTY));
        }

        @Override
        protected void onContentsChanged(int index) {
            setChanged();
            syncToClient();
        }

        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return canBreak(resource, DeconstructionRules.of(resource));
        }
    }
}
