package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;
import java.util.concurrent.CompletableFuture;
import net.minecraftforge.common.util.FakePlayer;

record GameTestPayloadContext(FakePlayer player) implements PayloadContext {
    @Override
    public CompletableFuture<Void> enqueueWork(Runnable work) {
        work.run();
        return CompletableFuture.completedFuture(null);
    }
}
