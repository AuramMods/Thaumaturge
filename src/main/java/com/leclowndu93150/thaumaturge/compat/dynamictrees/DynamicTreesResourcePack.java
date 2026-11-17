package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.AddPackFindersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class DynamicTreesResourcePack {
    private DynamicTreesResourcePack() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !ModList.get().isLoaded("dynamictrees")) {
            return;
        }

        event.addPackFinders(
                TTIds.rl("resourcepacks/dynamictrees"),
                PackType.CLIENT_RESOURCES,
                Component.literal("Thaumaturge Dynamic Trees"),
                PackSource.BUILT_IN,
                true,
                Pack.Position.TOP);
    }
}
