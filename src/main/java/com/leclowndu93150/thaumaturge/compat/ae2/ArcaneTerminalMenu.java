package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.core.definitions.AEItems;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantics;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.PlayerInternalInventory;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.content.workbench.InventoryArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

public final class ArcaneTerminalMenu extends MEStorageMenu implements IMenuCraftingPacket {
    private static final int GRID_SIZE = InventoryArcaneWorkbench.CRAFTING_SLOTS;
    private static final String CLEAR_GRID = "clearArcaneGrid";
    private static final String ENCODE_KNOWLEDGE = "encodeKnowledge";
    private static final String ENCODE_DISTILLATION = "encodeDistillation";

    public static final SlotSemantic CRYSTALS = SlotSemantics.register("THAUMATURGE_CRYSTALS", false);
    public static final SlotSemantic WAND = SlotSemantics.register("THAUMATURGE_WAND", false);

    private final ArcaneTerminalHost terminal;
    private final InternalInventory grid;
    private final CraftingTermSlot result;
    private int refreshDelay;

    @GuiSync(20)
    public Component craftCost = Component.empty();

    ArcaneTerminalMenu(MenuType<?> type, int id, Inventory player, ArcaneTerminalHost terminal) {
        super(type, id, player, terminal);
        this.terminal = terminal;
        var inventory = terminal.workbench();
        grid = inventory.getSubInventory(0, GRID_SIZE);
        for (int slot = 0; slot < grid.size(); slot++) {
            addSlot(new CraftingMatrixSlot(this, grid, slot), SlotSemantics.CRAFTING_GRID);
        }
        for (int i = 0; i < InventoryArcaneWorkbench.CRYSTAL_SLOTS; i++) {
            var expected = MenuArcaneWorkbench.PRIMAL_ORDER.get(i);
            addSlot(new AppEngSlot(inventory, GRID_SIZE + i) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    var aspect = EssentiaCrystalAccess.aspect(stack);
                    return super.mayPlace(stack) && aspect != null && aspect.is(expected);
                }
            }, CRYSTALS);
        }
        addSlot(new AppEngSlot(inventory, InventoryArcaneWorkbench.WAND_SLOT) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return super.mayPlace(stack) && SlotWorkbenchWand.isUsableWand(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        }, WAND);
        addSlot(result = new CraftingTermSlot(getPlayer(), getActionSource(), powerSource, storage, grid, grid, this) {
            @Override
            public void doClick(InventoryAction action, Player player) {
                if (player instanceof ServerPlayer server) craftInto(server, action);
            }
        }, SlotSemantics.CRAFTING_RESULT);
        if (terminal instanceof ArcaneTerminalPart) {
            addSlot(new AppEngSlot(terminal.getUpgrades(), 0), SlotSemantics.UPGRADE);
        }
        registerClientAction(CLEAR_GRID, this::clearGrid);
        registerClientAction(ENCODE_KNOWLEDGE, () -> encode(false));
        registerClientAction(ENCODE_DISTILLATION, () -> encode(true));
    }

    @Override
    public InternalInventory getCraftingMatrix() {
        return grid;
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    @Override
    public IGridNode getNetworkNode() {
        var node = super.getNetworkNode();
        var host = getActionHost();
        if (node == null && host != null) node = host.getActionableNode();
        return node != null && node.isActive() ? node : null;
    }

    @Override
    public void startAutoCrafting(List<AutoCraftEntry> entries) {
        if (getPlayer() instanceof ServerPlayer server && canInteractWithGrid() && getLocator() != null) {
            CraftConfirmMenu.openWithCraftingList(getActionHost(), server, getLocator(), entries);
        }
    }

    @Override
    public void slotsChanged(Container container) {
        refreshDelay = 0;
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide() && --refreshDelay <= 0) {
            refreshDelay = 5;
            refresh();
        }
        super.broadcastChanges();
    }

    public void clearGrid() {
        if (isClientSide()) {
            sendClientAction(CLEAR_GRID);
        } else if (getPlayer() instanceof ServerPlayer server) {
            var to = new PlayerInternalInventory(server.getInventory());
            for (int slot = 0; slot < grid.size(); slot++) {
                grid.setItemDirect(slot, to.addItems(grid.getStackInSlot(slot).copy()));
            }
            refresh();
        }
    }

    public void encode(boolean distillation) {
        if (isClientSide()) {
            sendClientAction(distillation ? ENCODE_DISTILLATION : ENCODE_KNOWLEDGE);
            return;
        }
        if (!(getPlayer() instanceof ServerPlayer player) || !canInteractWithGrid()) return;
        ItemStack[] slots = new ItemStack[GRID_SIZE];
        for (int slot = 0; slot < slots.length; slot++) slots[slot] = grid.getStackInSlot(slot);
        var inputs = List.of(slots);
        ItemStack blank = player.getInventory().items.stream()
                .filter(stack -> stack.is(AEItems.BLANK_PATTERN.asItem())).findFirst().orElse(ItemStack.EMPTY);
        if (blank.isEmpty()) {
            player.displayClientMessage(Component.translatable("gui.thaumaturge.arcane_terminal.blank_pattern"), true);
            return;
        }
        ItemStack output;
        if (distillation) {
            var occupied = inputs.stream().filter(stack -> !stack.isEmpty()).toList();
            output = occupied.size() == 1 ? ThaumicPatterns.distillation(occupied.get(0)) : ItemStack.EMPTY;
        } else {
            var job = new ArcaneTerminalCrafting(terminal, player, storage, powerSource, getActionSource());
            output = ThaumicPatterns.knowledge(player, inputs, job.inspect());
        }
        if (output.isEmpty()) {
            player.displayClientMessage(Component.translatable("gui.thaumaturge.arcane_terminal.no_pattern"), true);
            return;
        }
        blank.shrink(1);
        player.getInventory().placeItemBackInInventory(output);
        player.getInventory().setChanged();
    }

    private void refresh() {
        if (!(getPlayer() instanceof ServerPlayer player) || !canInteractWithGrid()) {
            result.set(ItemStack.EMPTY);
            craftCost = Component.empty();
            return;
        }
        var job = new ArcaneTerminalCrafting(terminal, player, storage, powerSource, getActionSource());
        var preview = job.preview();
        if (preview.successful() || preview.failure() == ArcaneCraftingTransaction.Failure.PAYMENT_UNAVAILABLE) {
            result.set(preview.output());
            var cost = preview.cost();
            craftCost = cost == null ? Component.empty() : Component.translatable(
                    preview.successful() ? "gui.thaumaturge.arcane_terminal.cost"
                            : "gui.thaumaturge.arcane_terminal.unpaid", cost.auraVis());
        } else {
            var level = player.serverLevel();
            var input = job.local().asVanillaCraftingInput(this);
            boolean vanilla = preview.failure() == ArcaneCraftingTransaction.Failure.NO_RECIPE;
            result.set(vanilla ? level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                    .map(recipe -> recipe.assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY) : ItemStack.EMPTY);
            craftCost = vanilla ? Component.empty() : Component.translatable("gui.thaumaturge.arcane_terminal.locked");
        }
    }

    private void craftInto(ServerPlayer player, InventoryAction action) {
        if (!canInteractWithGrid() || result.getItem().isEmpty()) return;
        ItemStack expected = result.getItem().copy();
        var to = action == InventoryAction.CRAFT_SHIFT || action == InventoryAction.CRAFT_ALL
                ? new PlayerInternalInventory(player.getInventory()) : new CarriedItemInventory(this);
        int count = action == InventoryAction.CRAFT_ITEM ? 1 : Math.max(1, expected.getMaxStackSize() / expected.getCount());
        for (int i = 0; i < count && ItemStack.matches(expected, result.getItem()); i++) {
            if (!to.simulateAdd(expected).isEmpty()) break;
            var crafted = craftOnce(player, expected);
            if (crafted.isEmpty()) break;
            var overflow = to.addItems(crafted);
            if (!overflow.isEmpty()) player.getInventory().placeItemBackInInventory(overflow);
            refresh();
        }
    }

    private ItemStack craftOnce(ServerPlayer player, ItemStack expected) {
        var job = new ArcaneTerminalCrafting(terminal, player, storage, powerSource, getActionSource());
        var vanillaInput = job.local().asVanillaCraftingInput(this);
        try (Transaction transaction = Transaction.openRoot()) {
            var craft = job.craft(transaction);
            if (!craft.successful()) {
                if (craft.failure() != ArcaneCraftingTransaction.Failure.NO_RECIPE) return ItemStack.EMPTY;
                var level = player.serverLevel();
                var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, vanillaInput, level).orElse(null);
                if (recipe == null || !ItemStack.matches(expected, recipe.assemble(vanillaInput, level.registryAccess()))) {
                    return ItemStack.EMPTY;
                }
                ForgeHooks.setCraftingPlayer(player);
                try {
                    var consumption = new IArcaneCraftingStore.Consumption(vanillaInput.getItems(),
                            recipe.getRemainingItems(vanillaInput), AspectList.EMPTY, job.local().wandStack());
                    if (!job.consumeLocal(consumption, 0, 0, 3, 3, transaction)) return ItemStack.EMPTY;
                } finally {
                    ForgeHooks.setCraftingPlayer(null);
                }
            } else if (!ItemStack.matches(craft.output(), expected)) {
                return ItemStack.EMPTY;
            }
            transaction.commit();
        }
        ItemStack output = expected.copy();
        output.onCraftedBy(player.level(), player, output.getCount());
        ForgeEventFactory.firePlayerCraftingEvent(player, output, vanillaInput);
        return output;
    }
}
