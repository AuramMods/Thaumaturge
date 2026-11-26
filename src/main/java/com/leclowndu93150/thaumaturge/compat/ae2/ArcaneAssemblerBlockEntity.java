package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageHelper;
import appeng.capabilities.Capabilities;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.transaction.RootCommitJournal;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.content.workbench.InventoryArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchCraftingStore;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

final class ArcaneAssemblerBlockEntity extends ThaumicGridBlockEntity implements ICraftingMachine {
    private static final String PENDING = "PendingCraft";
    private final List<ItemStack> pending = new ArrayList<>();
    private ItemStack wand = ItemStack.EMPTY;

    ArcaneAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumaturgeAe2.ARCANE_ASSEMBLER_ENTITY.get(), pos, state, 2.0);
        exposeCapability(Capabilities.CRAFTING_MACHINE, side -> this);
    }

    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        return new PatternContainerGroup(AEItemKey.of(getBlockState().getBlock().asItem()),
                Component.translatable(getBlockState().getBlock().getDescriptionId()), List.of());
    }

    @Override
    public boolean acceptsPlans() {
        return pending.isEmpty() && grid() != null;
    }

    @Override
    public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputs, Direction side) {
        if (!(level instanceof ServerLevel server) || !acceptsPlans()
                || !(pattern.getDefinition().getItem() instanceof KnowledgePatternItem)) return false;
        var tag = pattern.getDefinition().getTag();
        if (tag == null || !tag.contains(ThaumicPatterns.KNOWLEDGE, Tag.TAG_COMPOUND)) return false;
        var knowledge = tag.getCompound(ThaumicPatterns.KNOWLEDGE);
        if (!knowledge.hasUUID(ThaumicPatterns.CASTER)) return false;
        var caster = server.getServer().getPlayerList().getPlayer(knowledge.getUUID(ThaumicPatterns.CASTER));
        if (caster == null || caster.serverLevel() != server) return false;

        var gridSlots = knowledge.getList(ThaumicPatterns.GRID, Tag.TAG_COMPOUND);
        var crystals = knowledge.getList(ThaumicPatterns.CRYSTALS, Tag.TAG_COMPOUND);
        if (gridSlots.size() != InventoryArcaneWorkbench.CRAFTING_SLOTS
                || crystals.size() > InventoryArcaneWorkbench.CRYSTAL_SLOTS) return false;

        var workbench = new InventoryArcaneWorkbench();
        var supplied = new KeyCounter();
        for (var input : inputs) supplied.addAll(input);
        for (int slot = 0; slot < gridSlots.size(); slot++) {
            var ingredient = ItemStack.of(gridSlots.getCompound(slot));
            workbench.setItem(slot, ingredient);
            if (!ingredient.isEmpty()) supplied.remove(AEItemKey.of(ingredient), ingredient.getCount());
        }
        for (int slot = 0; slot < crystals.size(); slot++) {
            var crystal = ItemStack.of(crystals.getCompound(slot));
            if (crystal.isEmpty()) return false;
            workbench.setItem(InventoryArcaneWorkbench.CRAFTING_SLOTS + slot, crystal);
            supplied.remove(AEItemKey.of(crystal), crystal.getCount());
        }
        supplied.removeZeros();
        if (!supplied.isEmpty()) return false;
        workbench.setItem(InventoryArcaneWorkbench.WAND_SLOT, wand.copy());

        String location = server.dimension().location() + "/" + worldPosition.asLong() + "/arcane_assembler";
        UUID host = UUID.nameUUIDFromBytes(location.getBytes(StandardCharsets.UTF_8));
        var context = ArcaneWorkbenchContext.placed(caster, worldPosition, host, null);
        var positioned = workbench.asPositionedArcaneCraftInput();
        var input = positioned.input().withPlayer(caster);
        var store = new WorkbenchCraftingStore(workbench, caster, positioned.left(), positioned.top(),
                input.width(), input.height());

        try (Transaction transaction = Transaction.openRoot()) {
            var craft = ArcaneCraftingTransaction.craft(context, caster, input, store, transaction);
            if (!craft.successful() || !pattern.getPrimaryOutput().equals(GenericStack.fromItemStack(craft.output()))) {
                return false;
            }
            var produced = new ArrayList<ItemStack>();
            produced.add(craft.output());
            for (int slot = 0; slot < InventoryArcaneWorkbench.WAND_SLOT; slot++) {
                var remainder = workbench.getItem(slot);
                if (!remainder.isEmpty()) produced.add(remainder.copy());
            }
            new RootCommitJournal(() -> {
                wand = workbench.wandStack().copy();
                pending.addAll(produced);
                setChanged();
            }).updateSnapshots(transaction);
            transaction.commit();
        }
        return true;
    }

    void flushOutput() {
        var grid = grid();
        if (grid == null || pending.isEmpty()) return;
        var inventory = grid.getStorageService().getInventory();
        for (int index = 0; index < pending.size();) {
            var stack = pending.get(index);
            var key = AEItemKey.of(stack);
            int inserted = key == null ? 0 : (int) StorageHelper.poweredInsert(
                    grid.getEnergyService(), inventory, key, stack.getCount(), source, Actionable.MODULATE);
            stack.shrink(inserted);
            if (inserted > 0) setChanged();
            if (stack.isEmpty()) pending.remove(index);
            else index++;
        }
    }

    List<ItemStack> removeOutput() {
        var result = new ArrayList<>(pending);
        pending.clear();
        if (!wand.isEmpty()) result.add(exchangeWand(ItemStack.EMPTY));
        return result;
    }

    boolean hasWand() {
        return !wand.isEmpty();
    }

    ItemStack exchangeWand(ItemStack replacement) {
        var previous = wand;
        wand = replacement;
        setChanged();
        return previous;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        wand = ItemStack.of(input.getCompound("Wand"));
        pending.clear();
        var list = input.getList(PENDING, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            var stack = ItemStack.of(list.getCompound(index));
            if (!stack.isEmpty()) pending.add(stack);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (!wand.isEmpty()) output.put("Wand", wand.save(new CompoundTag()));
        var list = new ListTag();
        pending.forEach(stack -> list.add(stack.save(new CompoundTag())));
        output.put(PENDING, list);
    }
}
