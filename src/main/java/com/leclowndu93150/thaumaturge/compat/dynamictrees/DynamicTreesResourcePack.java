package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.resource.PathPackResources;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class DynamicTreesResourcePack {
    private DynamicTreesResourcePack() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        ModList mods = ModList.get();
        if (!mods.isLoaded(TTIds.DYNAMIC_TREES)) {
            return;
        }

        var path = mods.getModFileById(TTIds.MODID).getFile().findResource(TTIds.DYNAMIC_TREES_RESOURCE_PACK.getPath());
        PackType type = event.getPackType();
        Pack.Position position = type == PackType.CLIENT_RESOURCES ? Pack.Position.TOP : Pack.Position.BOTTOM;
        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate("mod/" + TTIds.DYNAMIC_TREES_RESOURCE_PACK,
                    Component.literal("Thaumaturge Dynamic Trees"), true,
                    id -> new PathPackResources(id, true, path), type, position, PackSource.BUILT_IN);
            if (pack != null) consumer.accept(pack);
        });
    }
}
