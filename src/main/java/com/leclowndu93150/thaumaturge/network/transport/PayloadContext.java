package com.leclowndu93150.thaumaturge.network.transport;

import java.util.concurrent.CompletableFuture;
import net.minecraft.world.entity.player.Player;

/** Gameplay packet context; transport implementations dispatch handlers on the game thread. */
public interface PayloadContext {
    Player player();

    CompletableFuture<Void> enqueueWork(Runnable work);
}
