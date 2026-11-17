package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class EffectTextures {
    private EffectTextures() {}

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) EffectTextures::preload);
    }

    private static void preload(ResourceManager manager) {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        for (String directory : new String[] {"textures/effect", "textures/misc"}) {
            for (ResourceLocation id : manager.listResources(
                            directory, path -> path.getPath().endsWith(".png"))
                    .keySet()) {
                if (id.getNamespace().equals(TTIds.MODID)) {
                    textures.getTexture(id);
                }
            }
        }
    }
}
