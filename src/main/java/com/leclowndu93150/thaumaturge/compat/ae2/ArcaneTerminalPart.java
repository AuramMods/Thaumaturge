package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.InventoryArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

final class ArcaneTerminalPart extends AbstractTerminalPart implements ArcaneTerminalHost, IGridTickable {
    private static final ResourceLocation OFF = TTIds.rl("part/arcane_terminal_off");
    private static final ResourceLocation ON = TTIds.rl("part/arcane_terminal_on");
    private static final IPartModel OFF_MODEL = new PartModel(MODEL_BASE, OFF, MODEL_STATUS_OFF);
    private static final IPartModel ON_MODEL = new PartModel(MODEL_BASE, ON, MODEL_STATUS_ON);
    private static final IPartModel CHANNEL_MODEL = new PartModel(MODEL_BASE, ON, MODEL_STATUS_HAS_CHANNEL);

    private final AppEngInternalInventory workbench = new AppEngInternalInventory(this, InventoryArcaneWorkbench.SIZE);
    private final IUpgradeInventory upgrades;

    ArcaneTerminalPart(IPartItem<?> item) {
        super(item);
        upgrades = UpgradeInventories.forMachine(item.asItem(), 1, this::wake);
        getMainNode().addService(IGridTickable.class, this);
    }

    static List<ResourceLocation> models() {
        return List.of(OFF, ON);
    }

    @Override
    public AppEngInternalInventory workbench() {
        return workbench;
    }

    @Override
    public ArcaneWorkbenchContext context(ServerPlayer player) {
        var pos = getBlockEntity().getBlockPos();
        String location = getLevel().dimension().location() + "/" + pos.asLong() + "/" + getSide();
        UUID identity = UUID.nameUUIDFromBytes(location.getBytes(StandardCharsets.UTF_8));
        return ArcaneWorkbenchContext.placed(player, pos, identity, null);
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    private void wake() {
        saveChanges();
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    @Override
    public void onChangeInventory(InternalInventory inventory, int slot) {
        super.onChangeInventory(inventory, slot);
        if (inventory == workbench && slot == InventoryArcaneWorkbench.WAND_SLOT) wake();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(20, 40, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        var level = getLevel();
        var wand = workbench.getStackInSlot(InventoryArcaneWorkbench.WAND_SLOT).copy();
        if (level == null || !node.isActive() || !upgrades.isInstalled(ThaumaturgeAe2.ARCANE_CHARGING_CARD.get())
                || !SlotWorkbenchWand.isUsableWand(wand)) return TickRateModulation.SLOWER;
        int max = WandVisHelper.getMaxVis(wand);
        var pos = getBlockEntity().getBlockPos();
        for (var aspect : MenuArcaneWorkbench.PRIMAL_ORDER) {
            if (max - WandVisHelper.getVis(wand, aspect) < 100 || AuraHelper.drainVis(level, pos, 1, true) < 1) continue;
            float drained = AuraHelper.drainVis(level, pos, 1, false);
            if (drained < 1) {
                if (drained > 0) AuraHelper.addVis(level, pos, drained);
                return TickRateModulation.SLOWER;
            }
            WandVisHelper.addVis(wand, aspect, 1, true);
            workbench.setItemDirect(InventoryArcaneWorkbench.WAND_SLOT, wand);
            return TickRateModulation.FASTER;
        }
        return TickRateModulation.SLOWER;
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        workbench.forEach(stack -> { if (!stack.isEmpty()) drops.add(stack); });
        upgrades.forEach(stack -> { if (!stack.isEmpty()) drops.add(stack); });
    }

    @Override
    public void clearContent() {
        super.clearContent();
        workbench.clear();
        upgrades.clear();
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        workbench.readFromNBT(tag, "arcaneWorkbench");
        upgrades.readFromNBT(tag, "arcaneUpgrades");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        workbench.writeToNBT(tag, "arcaneWorkbench");
        upgrades.writeToNBT(tag, "arcaneUpgrades");
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return ThaumaturgeAe2.ARCANE_TERMINAL_MENU.get();
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(OFF_MODEL, ON_MODEL, CHANNEL_MODEL);
    }
}
