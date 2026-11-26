package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.storage.ITerminalHost;
import appeng.api.stacks.AEKey;
import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.world.entity.player.Inventory;

public final class EssentiaTerminalMenu extends MEStorageMenu {
    public EssentiaTerminalMenu(int id, Inventory inventory, ITerminalHost host) {
        super(ThaumaturgeAe2.ESSENTIA_TERMINAL_MENU.get(), id, inventory, host);
    }

    @Override
    public boolean isKeyVisible(AEKey key) {
        return key instanceof EssentiaKey;
    }

    @Override
    public boolean canConfigureTypeFilter() {
        return false;
    }

    @Override
    protected void putCarriedItemIntoNetwork(boolean singleItem) {
        // This terminal only handles the essentia inside vessels.
    }
}
