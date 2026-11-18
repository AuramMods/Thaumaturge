package com.leclowndu93150.thaumaturge.server.command.admin;

import net.minecraft.world.entity.Entity;

/** Identifies specimens using persisted and client-visible vanilla entity data. */
public final class ShowcaseMarker {
    private static final String MARKER = "thaumaturge_showcase";

    private ShowcaseMarker() {}

    static void apply(Entity entity) {
        entity.addTag(MARKER);
        // The name style reaches clients through vanilla entity data.
        entity.setCustomName(entity.getType().getDescription().copy().withStyle(style -> style.withInsertion(MARKER)));
    }

    public static boolean isPresent(Entity entity) {
        var name = entity.getCustomName();
        return entity.getTags().contains(MARKER)
                || name != null && MARKER.equals(name.getStyle().getInsertion());
    }
}
