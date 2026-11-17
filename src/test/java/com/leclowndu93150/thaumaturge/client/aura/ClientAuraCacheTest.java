package com.leclowndu93150.thaumaturge.client.aura;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientAuraCacheTest {
    private final ChunkPos pos = new ChunkPos(3, -5);
    private IEventBus bus;

    @BeforeEach
    void registerTheActualProductionSubscriber() {
        ClientAuraCache.clear();
        bus = BusBuilder.builder().build();
        bus.register(ClientAuraCache.class);
    }

    @AfterEach
    void cleanUp() {
        bus.unregister(ClientAuraCache.class);
        ClientAuraCache.clear();
    }

    private void tick(int count) {
        for (int i = 0; i < count; i++) {
            bus.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
            bus.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
        }
    }

    @Test
    void nativeForgeBusAdvancesTheActualCacheOnlyAtEnd() {
        ClientAuraCache.put(pos, (short) 40, 30F, 2F);
        assertEquals(0L, ClientAuraCache.get(pos).tick());
        bus.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
        ClientAuraCache.put(pos, (short) 40, 30F, 2F);
        assertEquals(0L, ClientAuraCache.get(pos).tick());
        bus.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
        ClientAuraCache.put(pos, (short) 40, 30F, 2F);
        assertEquals(1L, ClientAuraCache.get(pos).tick());
        tick(3);
        ClientAuraCache.put(pos, (short) 40, 30F, 2F);
        assertEquals(4L, ClientAuraCache.get(pos).tick());
    }

    @Test
    void pairedNativePhasesPreserveTheTwentyTickRequestThrottle() {
        assertTrue(ClientAuraCache.shouldRequest(pos));
        tick(19);
        assertFalse(ClientAuraCache.shouldRequest(pos));
        tick(1);
        assertTrue(ClientAuraCache.shouldRequest(pos));
        assertFalse(ClientAuraCache.shouldRequest(pos));
    }

    @Test
    void snapshotStaysFreshThroughTheSixtiethTick() {
        ClientAuraCache.put(pos, (short) 40, 30F, 2F);
        tick(60);
        assertFalse(ClientAuraCache.shouldRequest(pos));
        tick(1);
        assertTrue(ClientAuraCache.shouldRequest(pos));
    }

    @Test
    void clearingAWorldDropsSnapshotsRequestsAndTheTickClock() {
        ClientAuraCache.shouldRequest(pos);
        tick(12);
        ClientAuraCache.put(pos, (short) 40, 30F, 2F);
        assertNotNull(ClientAuraCache.latest());
        ClientAuraCache.clear();
        assertNull(ClientAuraCache.latest());
        assertNull(ClientAuraCache.get(pos));
        assertTrue(ClientAuraCache.shouldRequest(pos));
        ClientAuraCache.put(pos, (short) 40, 30F, 2F);
        assertEquals(0L, ClientAuraCache.get(pos).tick());
    }
}
