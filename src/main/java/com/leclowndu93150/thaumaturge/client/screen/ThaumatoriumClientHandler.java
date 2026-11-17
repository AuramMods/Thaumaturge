package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.MenuThaumatorium;
import com.leclowndu93150.thaumaturge.network.ClientboundThaumatoriumRecipesPayload;
import net.minecraft.client.Minecraft;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public final class ThaumatoriumClientHandler {
    private ThaumatoriumClientHandler() {}

    public static void handle(ClientboundThaumatoriumRecipesPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null
                    && mc.player.containerMenu instanceof MenuThaumatorium menu
                    && menu.containerId == payload.containerId()) {
                menu.clientRecipes = payload.entries();
            }
        });
    }
}
