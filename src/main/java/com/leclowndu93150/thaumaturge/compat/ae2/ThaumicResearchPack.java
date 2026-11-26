package com.leclowndu93150.thaumaturge.compat.ae2;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.resource.PathPackResources;

final class ThaumicResearchPack {
    private ThaumicResearchPack() {}

    static void register(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        var path = ModList.get().getModFileById(TTIds.MODID).getFile().findResource("thaumic_energistics_data");
        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate("mod/thaumaturge_thaumic_energistics",
                    Component.translatable("thaumaturge.pack.thaumic_energistics"), true,
                    id -> new PathPackResources(id, true, path), PackType.SERVER_DATA, Pack.Position.BOTTOM,
                    PackSource.BUILT_IN);
            if (pack != null) consumer.accept(pack);
        });
    }
}
