package com.leclowndu93150.thaumaturge.content.equipment.hover;

import net.minecraft.world.entity.player.Player;

/** Transient server-side flight permission contributed by equipped hover gear. */
public interface HoverFlight {
    boolean thaumaturge$hasHoverFlight();

    void thaumaturge$setHoverFlight(boolean granted);

    static boolean mayFly(Player player, boolean nativePermission) {
        return nativePermission || player instanceof HoverFlight hover && hover.thaumaturge$hasHoverFlight();
    }
}
