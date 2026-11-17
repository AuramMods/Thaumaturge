package com.leclowndu93150.thaumaturge.event.tick;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Shared expectations for the actual transformed client/server non-passenger methods. */
public abstract class EntityTickRuntimeCases {
    private final List<String> trace = new ArrayList<>();
    private final Listener listener = new Listener();
    private Consumer<Entity> ticker;

    protected abstract Consumer<Entity> createTicker();
    private ItemEntity entity;

    public final class Listener {
        boolean cancel;
        Integer freezeAt;

        @SubscribeEvent
        public void before(EntityTickEvent.Pre event) {
            assertSame(entity, event.getEntity());
            trace.add("pre:" + event.getEntity().tickCount);
            if (freezeAt != null) event.getEntity().tickCount = freezeAt;
            event.setCanceled(cancel);
        }

        @SubscribeEvent
        public void after(EntityTickEvent.Post event) {
            assertSame(entity, event.getEntity());
            trace.add("post:" + event.getEntity().tickCount);
        }
    }

    @BeforeEach
    void setUp() {
        // A normal mod launch starts this bus during loading. This focused
        // entry point performs no mod construction, so start it explicitly.
        MinecraftForge.EVENT_BUS.start();
        MinecraftForge.EVENT_BUS.register(listener);
        ticker = createTicker();
        entity = mock(ItemEntity.class);
        when(entity.canUpdate()).thenReturn(true);
        when(entity.getPassengers()).thenReturn(List.of());
        doReturn(EntityType.ITEM).when(entity).getType();
        doAnswer(call -> {
            trace.add("tick:" + entity.tickCount);
            return null;
        }).when(entity).tick();
    }

    @AfterEach
    void tearDown() {
        MinecraftForge.EVENT_BUS.unregister(listener);
    }

    @Test
    void nativeNonLivingTickRunsPreBodyPostAfterTheCounterIncrement() {
        ticker.accept(entity);
        assertEquals(List.of("pre:1", "tick:1", "post:1"), trace);
        verify(entity).setOldPosAndRot();
        verify(entity).tick();
        assertEquals(1, entity.tickCount);
    }

    @Test
    void cancellationSkipsBodyAndPostWhileRetainingPassengerTraversal() {
        listener.cancel = true;
        listener.freezeAt = 99;
        ServerPlayer passenger = mock(ServerPlayer.class);
        when(entity.getPassengers()).thenReturn(List.of(passenger));
        when(passenger.getVehicle()).thenReturn(entity);
        when(passenger.canUpdate()).thenReturn(true);
        when(passenger.getPassengers()).thenReturn(List.of());
        doReturn(EntityType.PLAYER).when(passenger).getType();
        doAnswer(call -> {
            trace.add("ride:" + passenger.tickCount);
            return null;
        }).when(passenger).rideTick();
        ticker.accept(entity);
        assertEquals(List.of("pre:1", "ride:1"), trace);
        assertEquals(99, entity.tickCount);
        verify(entity, never()).tick();
        verify(passenger).rideTick();
        verify(passenger, never()).tick();
        assertEquals(1, passenger.tickCount);
    }

    @Test
    void nativeCanUpdateGuardPreventsAllTickPhases() {
        when(entity.canUpdate()).thenReturn(false);
        ticker.accept(entity);
        assertTrue(trace.isEmpty());
        assertEquals(1, entity.tickCount);
        verify(entity, never()).tick();
    }

    @Test
    void aFailedEntityTickPropagatesAndDoesNotPostSuccess() {
        var failure = new IllegalStateException("fixture tick failed");
        doThrow(failure).when(entity).tick();
        assertSame(failure, assertThrows(IllegalStateException.class, () -> ticker.accept(entity)));
        assertEquals(List.of("pre:1"), trace);
    }

    @Test
    void everyNativeTickPostsOnePairWithoutAccumulatingState() {
        ticker.accept(entity);
        ticker.accept(entity);
        assertEquals(List.of("pre:1", "tick:1", "post:1", "pre:2", "tick:2", "post:2"), trace);
        verify(entity, times(2)).tick();
    }

    @Test
    void postObservesTheSameEntityAfterItsTickMutations() {
        doAnswer(call -> {
            entity.tickCount = 41;
            return null;
        }).when(entity).tick();
        ticker.accept(entity);
        assertEquals(List.of("pre:1", "post:41"), trace);
    }

    @Test
    void onlyThePreEventIsCancelableOnTheActualForgeBus() {
        EntityTickEvent.Pre pre = new EntityTickEvent.Pre(entity);
        EntityTickEvent.Post post = new EntityTickEvent.Post(entity);
        assertTrue(pre.isCancelable());
        assertFalse(post.isCancelable());
        pre.setCanceled(true);
        assertTrue(pre.isCanceled());
        assertThrows(UnsupportedOperationException.class, () -> post.setCanceled(true));
        assertSame(entity, ((EntityTickEvent) pre).getEntity());
    }
}
