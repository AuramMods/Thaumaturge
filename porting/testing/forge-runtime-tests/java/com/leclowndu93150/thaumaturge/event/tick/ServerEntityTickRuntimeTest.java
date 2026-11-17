package com.leclowndu93150.thaumaturge.event.tick;

import static org.mockito.Mockito.*;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;

public class ServerEntityTickRuntimeTest extends EntityTickRuntimeCases {
    @Override
    protected Consumer<Entity> createTicker() {
        ServerLevel level = mock(ServerLevel.class, CALLS_REAL_METHODS);
        doReturn(InactiveProfiler.INSTANCE).when(level).getProfiler();
        return level::tickNonPassenger;
    }
}
