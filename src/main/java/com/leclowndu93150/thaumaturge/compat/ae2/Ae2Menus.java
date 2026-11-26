package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.NetworkHooks;

/** Opens Thaumaturge menu types with AE2's locators and submenu navigation. */
final class Ae2Menus {
    private Ae2Menus() {}

    static <T extends AEBaseMenu, H> MenuType<T> create(
            Class<H> hostType, Factory<T, H> factory, Function<H, Component> title) {
        MenuType<T> type = IForgeMenuType.create((id, inventory, buffer) -> {
            var locator = MenuLocators.readFromPacket(buffer);
            H host = locator.locate(inventory.player, hostType);
            if (host == null) throw new IllegalStateException("Missing AE2 menu host at " + locator);
            T menu = factory.create(id, inventory, host);
            menu.setReturnedFromSubScreen(buffer.readBoolean());
            return menu;
        });

        MenuOpener.addOpener(type, (player, locator, fromSubmenu) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            H host = locator.locate(player, hostType);
            if (host == null) return false;
            var provider = new SimpleMenuProvider((id, inventory, ignored) -> {
                T menu = factory.create(id, inventory, host);
                menu.setLocator(locator);
                return menu;
            }, title.apply(host));
            NetworkHooks.openScreen(serverPlayer, provider, buffer -> {
                MenuLocators.writeToPacket(buffer, locator);
                buffer.writeBoolean(fromSubmenu);
            });
            return true;
        });

        return type;
    }

    @FunctionalInterface
    interface Factory<T, H> {
        T create(int id, Inventory inventory, H host);
    }
}
