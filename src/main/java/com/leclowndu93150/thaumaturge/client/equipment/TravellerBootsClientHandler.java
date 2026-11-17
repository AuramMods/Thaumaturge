package com.leclowndu93150.thaumaturge.client.equipment;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.equipment.TravellerBootsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TravellerBootsClientHandler {
    private TravellerBootsClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive() || Minecraft.getInstance().isPaused()) {
            return;
        }
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.getItem() instanceof TravellerBootsItem) {
            TravellerBootsItem.clientMovementTick(player, boots);
        }
    }
}
