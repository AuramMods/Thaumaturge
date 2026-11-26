package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.storage.ITerminalHost;
import appeng.util.inv.AppEngInternalInventory;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import net.minecraft.server.level.ServerPlayer;

interface ArcaneTerminalHost extends ITerminalHost {
    AppEngInternalInventory workbench();

    ArcaneWorkbenchContext context(ServerPlayer player);
}
