package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTParticleSheets {
    private static final Map<ResourceLocation, ParticleSheet> SHEETS = new ConcurrentHashMap<>();

    private TTParticleSheets() {}

    public static ParticleSheet sheet(String name) {
        return sheet(TTIds.rl("textures/particle/" + name + ".png"));
    }

    public static ParticleSheet sheet(ResourceLocation texture) {
        return SHEETS.computeIfAbsent(texture, key -> new ParticleSheet(key.getPath(), key));
    }

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            SHEETS.values().forEach(ParticleSheet::invalidate);
            TextureManager textures = Minecraft.getInstance().getTextureManager();
            for (ParticleSheet sheet : SHEETS.values()) {
                if (!sheet.texture().getPath().startsWith("textures/particle/")) {
                    textures.getTexture(sheet.texture());
                }
            }
            for (ResourceLocation id : manager.listResources(
                            "textures/particle", path -> path.getPath().endsWith(".png"))
                    .keySet()) {
                textures.getTexture(id);
            }
        });
    }
}
