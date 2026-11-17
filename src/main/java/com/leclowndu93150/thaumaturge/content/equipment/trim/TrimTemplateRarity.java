package com.leclowndu93150.thaumaturge.content.equipment.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID)
public final class TrimTemplateRarity {
    private TrimTemplateRarity() {}

    @SubscribeEvent
    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(
                TTItems.ARCANE_ARMOR_TRIM_SMITHING_TEMPLATE.get(),
                builder -> builder.set(DataComponents.RARITY, Rarity.UNCOMMON));
        event.modify(
                TTItems.OCCULT_ARMOR_TRIM_SMITHING_TEMPLATE.get(),
                builder -> builder.set(DataComponents.RARITY, Rarity.RARE));
    }
}
