package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.network.ClientboundGolemPressStuffPayload;
import net.minecraft.client.Minecraft;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public final class GolemPressClientHandler {
    private GolemPressClientHandler() {}

    public static void handle(ClientboundGolemPressStuffPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.getBlockEntity(payload.pos()) instanceof BlockEntityGolemBuilder builder) {
                boolean[] stuff = new boolean[payload.stuff().length];
                for (int i = 0; i < stuff.length; i++) {
                    stuff[i] = payload.stuff()[i] == 1;
                }
                builder.hasStuff = stuff;
            }
        });
    }
}
