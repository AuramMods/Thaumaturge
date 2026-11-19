package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaItemStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class TTCapabilities {
    private TTCapabilities() {}

    @SubscribeEvent
    @SuppressWarnings("removal")
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IEssentiaConduit.class);
        event.register(IEssentiaTransport.class);
        event.register(IEssentiaStorage.class);
        event.register(IAspectQuery.class);
        event.register(IItemEssentia.class);
        event.register(IEssentiaItemStorage.class);
        event.register(IAspectContainer.class);
        event.register(IVisRelaySource.class);
    }
}
