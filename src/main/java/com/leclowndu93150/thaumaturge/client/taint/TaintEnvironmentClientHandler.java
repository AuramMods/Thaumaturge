package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.network.ClientboundTaintEnvironmentPayload;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;

public final class TaintEnvironmentClientHandler {
    private TaintEnvironmentClientHandler() {}

    public static void handle(ClientboundTaintEnvironmentPayload payload, PayloadContext context) {
        context.enqueueWork(() -> TaintEnvironmentClientState.accept(payload.pressure()));
    }
}
