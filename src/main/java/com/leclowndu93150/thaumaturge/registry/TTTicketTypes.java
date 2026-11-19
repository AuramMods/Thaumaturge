package com.leclowndu93150.thaumaturge.registry;

import java.util.Comparator;
import java.util.UUID;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public final class TTTicketTypes {
    public static final int TRANSIT_LIFETIME = 600;
    private static final int ENCOUNTER_LIFETIME = 100;
    public static final TicketType<UUID> LABYRINTH_TRANSIT = TicketType.create(
            "thaumaturge:labyrinth_transit", Comparator.<UUID>naturalOrder(), TRANSIT_LIFETIME);
    public static final TicketType<ChunkPos> LABYRINTH_ENCOUNTER = TicketType.create(
            "thaumaturge:labyrinth_encounter", Comparator.comparingLong(ChunkPos::toLong), ENCOUNTER_LIFETIME);

    private TTTicketTypes() {}
}
