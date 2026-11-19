package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class CultistArmorDyeing {
    private CultistArmorDyeing() {}

    public static List<Item> dyeableArmor() {
        return List.of(
                TTItems.CRIMSON_PLATE_HELM.get(),
                TTItems.CRIMSON_PLATE_CHEST.get(),
                TTItems.CRIMSON_PLATE_LEGS.get(),
                TTItems.CRIMSON_RANGER_HELM.get(),
                TTItems.CRIMSON_RANGER_CHEST.get(),
                TTItems.CRIMSON_RANGER_LEGS.get(),
                TTItems.CRIMSON_CAPTAIN_HELM.get(),
                TTItems.CRIMSON_CAPTAIN_CHEST.get(),
                TTItems.CRIMSON_ROBE_HELM.get(),
                TTItems.CRIMSON_ROBE_CHEST.get(),
                TTItems.CRIMSON_ROBE_LEGS.get(),
                TTItems.CRIMSON_PRAETOR_HELM.get(),
                TTItems.CRIMSON_PRAETOR_CHEST.get(),
                TTItems.CRIMSON_PRAETOR_LEGS.get());
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (Item item : dyeableArmor()) {
                CauldronInteraction.WATER.put(item, CauldronInteraction.DYED_ITEM);
            }
        });
    }
}
