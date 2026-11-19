package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ModelEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class ConnectedModelRegistration {
    private ConnectedModelRegistration() {}

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ConnectedSheetModel.TYPE.getPath(), new ConnectedModelLoader<>(ConnectedSheetModel.CODEC));
        event.register(ConnectedCornersModel.TYPE.getPath(), new ConnectedModelLoader<>(ConnectedCornersModel.CODEC));
        event.register(ConnectedStairsModel.TYPE.getPath(), new ConnectedModelLoader<>(ConnectedStairsModel.CODEC));
    }
}
