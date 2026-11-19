package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.donator.DonatorCapeClient;
import com.leclowndu93150.thaumaturge.client.screen.config.ThaumaturgeConfigScreen;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.compat.sable.SableClientSpaces;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ThaumaturgeClient {
    private ThaumaturgeClient() {}

    public static void register() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(DonatorCapeClient::onConfigReloading);
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenFactory.class, () -> new ConfigScreenFactory(ThaumaturgeConfigScreen::new));
        if (ModList.get().isLoaded(TTIds.SABLE)) {
            ClientSpaces.install(new SableClientSpaces());
        }
    }
}
