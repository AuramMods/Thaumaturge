package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.client.AspectRendering;
import com.leclowndu93150.thaumaturge.api.wands.render.WandRenderers;
import com.leclowndu93150.thaumaturge.client.model.WandRendererRegistry;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectRenderingBindings;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTClientBindings {
    private TTClientBindings() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        WandRenderers.bind(new WandRendererRegistry());
        AspectRendering.bind(new AspectRenderingBindings());
        AspectKnowledgeAccess.bind(aspect -> {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player == null || !minecraft.isSameThread() || AspectPools.isDiscovered(player, aspect)) {
                return AspectKnowledge.KNOWN;
            }
            boolean derivable = !aspect.value().isPrimal() && AspectPools.hasDiscoveredComponents(player, aspect);
            return derivable ? AspectKnowledge.DEDUCIBLE : AspectKnowledge.UNKNOWN;
        });
    }
}
