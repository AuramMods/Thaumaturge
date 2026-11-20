package com.leclowndu93150.thaumaturge.client.taint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.ClientSessionReset;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaintEnvironmentSessionTest {
    private IEventBus bus;

    @BeforeEach
    void registerCleanup() {
        bus = BusBuilder.builder().build();
        bus.register(ClientSessionReset.class);
        TaintEnvironmentClientState.reset();
        TaintEnvironmentClientState.accept(0.8F);
        TaintEnvironmentClientState.tick();
        assertTrue(TaintEnvironmentClientState.pressure() > 0.0F);
    }

    @AfterEach
    void resetState() {
        bus.unregister(ClientSessionReset.class);
        TaintEnvironmentClientState.reset();
    }

    @Test
    void clientDisconnectClearsTaintEvenWithoutAnActivePlayer() {
        bus.post(new ClientPlayerNetworkEvent.LoggingOut(null, null, null));
        assertTaintCleared();
    }

    @Test
    void clientLevelUnloadClearsTaintWhileServerLevelUnloadKeepsIt() {
        LevelAccessor level = mock(LevelAccessor.class);
        float displayed = TaintEnvironmentClientState.pressure();
        bus.post(new LevelEvent.Unload(level));
        assertEquals(displayed, TaintEnvironmentClientState.pressure());

        when(level.isClientSide()).thenReturn(true);
        bus.post(new LevelEvent.Unload(level));
        assertTaintCleared();
    }

    private static void assertTaintCleared() {
        assertEquals(0.0F, TaintEnvironmentClientState.pressure());
        TaintEnvironmentClientState.tick();
        assertEquals(0.0F, TaintEnvironmentClientState.pressure());
    }
}
