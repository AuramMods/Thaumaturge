package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class TransitState {
    private Optional<Pending> pending = Optional.empty();
    private long cooldownUntil;
    private long lastNotice;
    private ServerLevel contactedLevel;
    private int portalTime;

    Optional<Pending> pending() {
        return pending;
    }

    void setPending(Optional<Pending> pending) {
        this.pending = pending;
    }

    long cooldownUntil() {
        return cooldownUntil;
    }

    void setCooldownUntil(long cooldownUntil) {
        this.cooldownUntil = cooldownUntil;
    }

    boolean noticeDue(long gameTime, long interval) {
        if (gameTime - lastNotice < interval) {
            return false;
        }
        lastNotice = gameTime;
        return true;
    }

    void markContact(ServerLevel level) {
        contactedLevel = level;
    }

    void resetContact() {
        contactedLevel = null;
        portalTime = 0;
    }

    boolean tickPortal(ServerLevel level, boolean canUsePortal, int warmupTicks) {
        boolean contacted = contactedLevel == level;
        contactedLevel = null;
        if (!contacted) {
            portalTime = Math.max(0, portalTime - 4);
            return false;
        }
        // The original portal processor accumulates contact even while chunk preload postpones travel.
        return canUsePortal && portalTime++ >= warmupTicks;
    }

    record Pending(
            PortalLink link,
            ResourceKey<Level> level,
            ChunkPos chunk,
            int radius,
            CompletableFuture<?> loaded,
            long started) {}
}
