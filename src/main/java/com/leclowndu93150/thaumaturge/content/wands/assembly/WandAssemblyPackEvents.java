package com.leclowndu93150.thaumaturge.content.wands.assembly;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class WandAssemblyPackEvents {
    private WandAssemblyPackEvents() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        event.addRepositorySource(consumer -> {
            Pack.Info info = new Pack.Info(
                    Component.literal("Wand assembly recipes derived from the cap and rod registries"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA),
                    FeatureFlagSet.of());
            consumer.accept(Pack.create(TTIds.MODID + "_wand_assembly", Component.literal("Thaumaturge Wand Assembly"),
                    true, WandAssemblyPackResources::new, info, PackType.SERVER_DATA, Pack.Position.BOTTOM, false,
                    PackSource.BUILT_IN));
        });
    }
}
