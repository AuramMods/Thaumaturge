package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchAuraSourcesEvent;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchVisSourcesEvent;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayWorkbenchSource;
import com.leclowndu93150.thaumaturge.registry.TTWorkbenchSources;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class WorkbenchSourceRegistration {
    private WorkbenchSourceRegistration() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RegisterWorkbenchVisSourcesEvent visSources = new RegisterWorkbenchVisSourcesEvent();
            visSources.register(new VisRelayWorkbenchSource());
            ModLoader.postEvent(visSources);
            TTWorkbenchSources.registerVisSources(visSources.sources());

            RegisterWorkbenchAuraSourcesEvent auraSources = new RegisterWorkbenchAuraSourcesEvent();
            ModLoader.postEvent(auraSources);
            TTWorkbenchSources.registerAuraSources(auraSources.sources());
        });
    }
}
