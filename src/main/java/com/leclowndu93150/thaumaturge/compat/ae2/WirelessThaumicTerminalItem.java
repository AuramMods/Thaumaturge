package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.ISubMenu;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.content.workbench.InventoryArcaneWorkbench;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

final class WirelessThaumicTerminalItem extends WirelessTerminalItem {
    private final boolean arcane;

    WirelessThaumicTerminalItem(Properties properties, boolean arcane) {
        super(() -> 1_600_000, properties.stacksTo(1));
        this.arcane = arcane;
    }

    @Override
    public MenuType<?> getMenuType() {
        return arcane ? ThaumaturgeAe2.ARCANE_TERMINAL_MENU.get() : ThaumaturgeAe2.ESSENTIA_TERMINAL_MENU.get();
    }

    @Override
    public @Nullable ItemMenuHost getMenuHost(Player player, int slot, ItemStack stack, @Nullable BlockPos pos) {
        if (!arcane) return super.getMenuHost(player, slot, stack, pos);
        return new ArcaneHost(player, slot, stack, (p, submenu) -> openFromInventory(p, slot, true));
    }

    private static final class ArcaneHost extends WirelessTerminalMenuHost
            implements ArcaneTerminalHost, InternalInventoryHost {
        private static final String WORKBENCH = "arcaneWorkbench";
        private static final String ID = "arcaneWorkbenchId";
        private final Player player;
        private final AppEngInternalInventory workbench;

        ArcaneHost(Player player, int slot, ItemStack stack, BiConsumer<Player, ISubMenu> returnToTerminal) {
            super(player, slot, stack, returnToTerminal);
            this.player = player;
            workbench = new AppEngInternalInventory(this, InventoryArcaneWorkbench.SIZE);
            if (stack.hasTag()) workbench.readFromNBT(stack.getTag(), WORKBENCH);
        }

        @Override
        public AppEngInternalInventory workbench() {
            return workbench;
        }

        @Override
        public ArcaneWorkbenchContext context(ServerPlayer actor) {
            var tag = getItemStack().getOrCreateTag();
            if (!tag.hasUUID(ID)) {
                tag.putUUID(ID, UUID.randomUUID());
                player.getInventory().setChanged();
            }
            return ArcaneWorkbenchContext.virtual(actor, tag.getUUID(ID), actor.getUUID());
        }

        @Override
        public void saveChanges() {
            if (!isClientSide()) {
                workbench.writeToNBT(getItemStack().getOrCreateTag(), WORKBENCH);
                player.getInventory().setChanged();
            }
        }

        @Override
        public void onChangeInventory(InternalInventory inventory, int slot) {
            saveChanges();
        }

        @Override
        public boolean isClientSide() {
            return player.level().isClientSide();
        }
    }
}
