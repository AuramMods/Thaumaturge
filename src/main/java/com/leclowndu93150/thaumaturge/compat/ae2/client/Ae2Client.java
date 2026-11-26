package com.leclowndu93150.thaumaturge.compat.ae2.client;

import appeng.api.client.AEKeyRendering;
import appeng.api.client.StorageCellModels;
import appeng.api.util.AEColor;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.render.StaticItemColor;
import appeng.init.client.InitScreens;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.ae2.EssentiaKey;
import com.leclowndu93150.thaumaturge.compat.ae2.EssentiaKeyType;
import com.leclowndu93150.thaumaturge.compat.ae2.EssentiaTerminalMenu;
import com.leclowndu93150.thaumaturge.compat.ae2.ThaumaturgeAe2;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class Ae2Client {
    private Ae2Client() {}

    public static void register(IEventBus bus) {
        bus.addListener(Ae2Client::setup);
        bus.addListener(Ae2Client::registerItemColors);
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new StaticItemColor(AEColor.TRANSPARENT), ThaumaturgeAe2.ESSENTIA_TERMINAL.get(),
                ThaumaturgeAe2.ESSENTIA_STORAGE_MONITOR.get(), ThaumaturgeAe2.ESSENTIA_CONVERSION_MONITOR.get(),
                ThaumaturgeAe2.ESSENTIA_LEVEL_EMITTER.get(), ThaumaturgeAe2.ARCANE_TERMINAL.get());
    }

    private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            AEKeyRendering.register(EssentiaKeyType.INSTANCE, EssentiaKey.class, new EssentiaKeyRenderer());
            InitScreens.<EssentiaTerminalMenu, MEStorageScreen<EssentiaTerminalMenu>>register(
                    ThaumaturgeAe2.ESSENTIA_TERMINAL_MENU.get(), MEStorageScreen::new,
                    "/screens/terminals/terminal.json");
            InitScreens.register(ThaumaturgeAe2.ARCANE_TERMINAL_MENU.get(), ArcaneTerminalScreen::new,
                    "/screens/terminals/thaumaturge_arcane_terminal.json");
            ThaumaturgeAe2.CELLS.forEach(cell ->
                    StorageCellModels.registerModel(cell.get(), TTIds.rl("block/drive/" + cell.getId().getPath())));
        });
    }
}
