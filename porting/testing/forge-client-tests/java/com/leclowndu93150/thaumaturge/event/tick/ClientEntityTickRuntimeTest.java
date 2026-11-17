package com.leclowndu93150.thaumaturge.event.tick;

import static org.mockito.Mockito.*;

import java.util.function.Consumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;

public class ClientEntityTickRuntimeTest extends EntityTickRuntimeCases {
    @Override
    protected Consumer<Entity> createTicker() {
        ClientLevel level = mock(ClientLevel.class, CALLS_REAL_METHODS);
        doReturn(InactiveProfiler.INSTANCE).when(level).getProfiler();
        return level::tickNonPassenger;
    }
}
