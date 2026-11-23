package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ServerPlayer;

record GameTestPayloadContext(ServerPlayer player) implements PayloadContext {
    @Override
    public CompletableFuture<Void> enqueueWork(Runnable work) {
        work.run();
        return CompletableFuture.completedFuture(null);
    }
}
