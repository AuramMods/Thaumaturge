package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class SmelterDataMaps {
    public static final DataMapType<Block, SmelterStats> SMELTER_STATS = DataMapType.builder(
                    TTIds.rl("smelter_stats"), Registries.BLOCK, SmelterStats.CODEC)
            .build();

    private SmelterDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(SMELTER_STATS);
    }
}
