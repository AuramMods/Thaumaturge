package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.screen.research.ThaumonomiconBrowserScreen;
import com.leclowndu93150.thaumaturge.network.ClientboundOpenThaumonomiconPayload;
import net.minecraft.client.Minecraft;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public final class OpenThaumonomiconHandler {
    private OpenThaumonomiconHandler() {}

    public static void handle(ClientboundOpenThaumonomiconPayload payload, PayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(ThaumonomiconBrowserScreen.reopen()));
    }
}
