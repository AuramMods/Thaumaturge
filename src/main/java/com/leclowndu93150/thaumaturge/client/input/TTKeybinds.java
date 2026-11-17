package com.leclowndu93150.thaumaturge.client.input;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.screen.research.ThaumonomiconBrowserScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTKeybinds {
    public static final String CATEGORY = "key.categories.thaumaturge";
    public static final KeyMapping OPEN_THAUMONOMICON =
            new KeyMapping("key.thaumaturge.thaumonomicon", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);
    public static final KeyMapping CHANGE_FOCUS =
            new KeyMapping("key.thaumaturge.change_focus", GLFW.GLFW_KEY_F, CATEGORY);
    public static final KeyMapping MISC_TOGGLE =
            new KeyMapping("key.thaumaturge.misc_toggle", GLFW.GLFW_KEY_G, CATEGORY);

    public static final KeyMapping TOGGLE_HOVER =
            new KeyMapping("key.thaumaturge.toggle_hover", GLFW.GLFW_KEY_H, CATEGORY);

    private TTKeybinds() {}

    public static void onRegisterMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_THAUMONOMICON);
        event.register(CHANGE_FOCUS);
        event.register(TOGGLE_HOVER);
        event.register(MISC_TOGGLE);
    }

    @EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterKeyMappingsEvent event) {
            onRegisterMappings(event);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().screen != null) return;
        while (OPEN_THAUMONOMICON.consumeClick()) {
            Minecraft.getInstance().setScreen(ThaumonomiconBrowserScreen.reopen());
        }
    }
}
