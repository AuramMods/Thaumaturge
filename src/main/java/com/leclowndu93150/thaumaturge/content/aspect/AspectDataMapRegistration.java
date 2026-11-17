package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class AspectDataMapRegistration {
    private AspectDataMapRegistration() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(AspectDataMaps.BASE_ASPECTS);
        event.register(AspectDataMaps.ENTITY_ASPECTS);
    }
}
