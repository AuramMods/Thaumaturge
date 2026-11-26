package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.api.transaction.RootCommitJournal;
import com.leclowndu93150.thaumaturge.api.transaction.SnapshotJournal;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.workbench.InventoryArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchCraftingStore;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** A native arcane workbench snapshot with optional payment from network crystals. */
final class ArcaneTerminalCrafting {
    private final ArcaneTerminalHost host;
    private final ServerPlayer player;
    private final MEStorage network;
    private final IEnergySource power;
    private final IActionSource source;
    private final InventoryArcaneWorkbench local = new InventoryArcaneWorkbench();
    private final ArcaneCraftingInput.Positioned positioned;
    private final ArcaneCraftingInput input;
    private final List<GenericStack> drawn = new ArrayList<>();

    private final SnapshotJournal<Integer> networkPayment = new SnapshotJournal<>() {
        @Override
        protected Integer createSnapshot() {
            return drawn.size();
        }

        @Override
        protected void revertToSnapshot(Integer size) {
            while (drawn.size() > size) {
                var entry = drawn.remove(drawn.size() - 1);
                long remaining = entry.amount()
                        - network.insert(entry.what(), entry.amount(), Actionable.MODULATE, source);
                if (remaining > 0 && entry.what() instanceof AEItemKey key) {
                    player.getInventory().placeItemBackInInventory(key.toStack((int) remaining));
                }
            }
        }
    };

    ArcaneTerminalCrafting(ArcaneTerminalHost host, ServerPlayer player, MEStorage network, IEnergySource power,
            IActionSource source) {
        this.host = host;
        this.player = player;
        this.network = network;
        this.power = power;
        this.source = source;
        var live = host.workbench();
        for (int slot = 0; slot < live.size(); slot++) local.setItem(slot, live.getStackInSlot(slot).copy());
        positioned = local.asPositionedArcaneCraftInput();
        var base = positioned.input();
        var crystals = availableCrystals();
        input = new ArcaneCraftingInput(base.width(), base.height(), base.items()) {
            @Override
            public AspectList availableCrystals() {
                return crystals;
            }
        }.withPlayer(player);
    }

    ArcaneCraftingTransaction.Result preview() {
        return ArcaneCraftingTransaction.preview(host.context(player), player, input);
    }

    ArcaneCraftingTransaction.Inspection inspect() {
        return ArcaneCraftingTransaction.inspect(host.context(player), player, input);
    }

    ArcaneCraftingTransaction.Result craft(TransactionContext transaction) {
        return ArcaneCraftingTransaction.craft(host.context(player), player, input, this::consume, transaction);
    }

    private AspectList availableCrystals() {
        AspectList crystals = local.availableCrystals();
        if (positioned.input().isEmpty()) return crystals;
        for (var entry : network.getAvailableStacks()) {
            if (!(entry.getKey() instanceof AEItemKey key) || entry.getLongValue() <= 0) continue;
            var aspect = EssentiaCrystalAccess.aspect(key.toStack());
            if (aspect != null) {
                long room = Integer.MAX_VALUE - crystals.amountOf(aspect);
                int added = (int) Math.min(room, entry.getLongValue());
                if (added > 0) crystals = crystals.add(aspect, added);
            }
        }
        return crystals;
    }

    private boolean consume(IArcaneCraftingStore.Consumption consumption, TransactionContext transaction) {
        AspectList localPayment = AspectList.EMPTY;
        AspectList loaded = local.availableCrystals();
        networkPayment.updateSnapshots(transaction);
        for (var cost : consumption.crystals().entries()) {
            int onHand = Math.min(cost.amount(), loaded.amountOf(cost.aspect()));
            if (onHand > 0) localPayment = localPayment.add(cost.aspect(), onHand);
            if (!extractCrystals(cost, cost.amount() - onHand)) return false;
        }
        var localConsumption = new IArcaneCraftingStore.Consumption(
                consumption.grid(), consumption.remainders(), localPayment, consumption.wand());
        return consumeLocal(localConsumption, positioned.left(), positioned.top(),
                input.width(), input.height(), transaction);
    }

    private boolean extractCrystals(AspectInstance cost, int remaining) {
        if (remaining <= 0) return true;
        for (var entry : network.getAvailableStacks()) {
            if (!(entry.getKey() instanceof AEItemKey key) || entry.getLongValue() <= 0
                    || !cost.aspect().equals(EssentiaCrystalAccess.aspect(key.toStack()))) continue;
            long extracted = StorageHelper.poweredExtraction(
                    power, network, key, Math.min(remaining, entry.getLongValue()), source);
            if (extracted > 0) {
                drawn.add(new GenericStack(key, extracted));
                remaining -= (int) extracted;
            }
            if (remaining <= 0) return true;
        }
        return false;
    }

    boolean consumeLocal(IArcaneCraftingStore.Consumption consumption,
            int left, int top, int width, int height, TransactionContext transaction) {
        var store = new WorkbenchCraftingStore(local, player, left, top, width, height);
        if (!store.consume(consumption, transaction)) return false;
        new RootCommitJournal(() -> {
            var live = host.workbench();
            for (int slot = 0; slot < live.size(); slot++) {
                ItemStack updated = local.getItem(slot);
                if (!ItemStack.matches(live.getStackInSlot(slot), updated)) live.setItemDirect(slot, updated.copy());
            }
        }).updateSnapshots(transaction);
        return true;
    }

    InventoryArcaneWorkbench local() {
        return local;
    }
}
