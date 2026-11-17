package com.leclowndu93150.thaumaturge.debug.network;

import com.leclowndu93150.thaumaturge.debug.client.RaycastDebugOverlay;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public class ToggleRaycastDebugHandler {

    public static void handle(ClientboundToggleRaycastDebugPayload payload, PayloadContext context) {
        context.enqueueWork(RaycastDebugOverlay::toggle);
    }

    public static void handleServerRaycast(ClientboundRaycastDebugPayload payload, PayloadContext context) {
        context.enqueueWork(() -> {
            RaycastDebugOverlay.serverHitResult = payload.result();
        });
    }
}
