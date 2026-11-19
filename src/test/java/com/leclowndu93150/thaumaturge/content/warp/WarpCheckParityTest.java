package com.leclowndu93150.thaumaturge.content.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.state.AttachmentSyncPayload;
import com.leclowndu93150.thaumaturge.testing.AttachmentFixture;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

class WarpCheckParityTest {
    @Test
    void idleCheckDoesNotConsumeRandomnessAndStillSynchronizesWarp() {
        Fixture fixture = fixture(0, 0, 0, 0);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 0);
                MockedStatic<PayloadNetwork> packets = mockStatic(PayloadNetwork.class)) {
            WarpEvents.checkWarpEvent(fixture.player());

            verifyNoInteractions(fixture.random());
            assertSynchronizedCounter(fixture, packets, 0);
        }
    }

    @Test
    void chanceImmediatelyBelowOnePercentStepRunsCheckAndSyncsAfterCounter() {
        Fixture fixture = fixture(0, 1, 0, 1);
        when(fixture.random().nextDouble()).thenReturn(Math.nextDown(0.02));
        when(fixture.random().nextInt(7)).thenReturn(0);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 9);
                MockedStatic<PlayerResearchAccess> researchAccess = researchAccess(fixture);
                MockedStatic<PayloadNetwork> packets = mockStatic(PayloadNetwork.class)) {
            WarpEvents.checkWarpEvent(fixture.player());

            InOrder order = inOrder(fixture.random(), fixture.data());
            order.verify(fixture.random()).nextDouble();
            order.verify(fixture.random()).nextInt(7);
            order.verify(fixture.data()).setCounter(-4);
            assertSynchronizedCounter(fixture, packets, -4);
            warpManager.verify(() -> WarpManager.sendActionBar(fixture.player(), "warp.thaumaturge.text.11"));
        }
    }

    @Test
    void chanceAtOnePercentStepDoesNotRunStrengthRollButStillSynchronizes() {
        Fixture fixture = fixture(0, 1, 0, 1);
        when(fixture.random().nextDouble()).thenReturn(0.02);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 9);
                MockedStatic<PayloadNetwork> packets = mockStatic(PayloadNetwork.class)) {
            WarpEvents.checkWarpEvent(fixture.player());

            verify(fixture.random()).nextDouble();
            verify(fixture.random(), never()).nextInt(anyInt());
            verify(fixture.data(), never()).setCounter(anyInt());
            assertSynchronizedCounter(fixture, packets, 1);
        }
    }

    @Test
    void largeCounterUsesCappedWideEffectiveWarpAndPreservesCounterFormula() {
        Fixture fixture = fixture(0, 1, 0, Integer.MAX_VALUE);
        when(fixture.random().nextDouble()).thenReturn(0.5);
        when(fixture.random().nextInt(100)).thenReturn(0);
        int expectedCounter = (int)
                Math.min(Integer.MAX_VALUE - 5.0, Integer.MAX_VALUE - 2.0 * Math.sqrt(Integer.MAX_VALUE) + 2.0 * 9);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 9);
                MockedStatic<PlayerResearchAccess> researchAccess = researchAccess(fixture);
                MockedStatic<PayloadNetwork> packets = mockStatic(PayloadNetwork.class)) {
            WarpEvents.checkWarpEvent(fixture.player());

            InOrder order = inOrder(fixture.random(), fixture.data());
            order.verify(fixture.random()).nextDouble();
            order.verify(fixture.random()).nextInt(100);
            order.verify(fixture.data()).setCounter(expectedCounter);
            assertSynchronizedCounter(fixture, packets, expectedCounter);
        }
    }

    @Test
    void largeGearUsesWideRawArithmeticAndKeepsTheEffectiveWarpCap() {
        Fixture fixture = fixture(0, 1, 0, 1);
        when(fixture.random().nextDouble()).thenReturn(0.0);
        when(fixture.random().nextInt(100)).thenReturn(0);

        try (MockedStatic<WarpManager> warpManager = warpManager(fixture, 1_500_000_000);
                MockedStatic<PlayerResearchAccess> researchAccess = researchAccess(fixture);
                MockedStatic<GuardianSpawner> guardianSpawner = mockStatic(GuardianSpawner.class);
                MockedStatic<PayloadNetwork> packets = mockStatic(PayloadNetwork.class)) {
            WarpEvents.checkWarpEvent(fixture.player());

            InOrder order = inOrder(fixture.random(), fixture.data());
            order.verify(fixture.random()).nextDouble();
            order.verify(fixture.random()).nextInt(100);
            order.verify(fixture.data()).setCounter(-4);
            assertSynchronizedCounter(fixture, packets, -4);
            guardianSpawner.verify(() -> GuardianSpawner.spawn(fixture.player(), 6));
        }
    }

    private static Fixture fixture(int temporary, int normal, int permanent, int counter) {
        ServerPlayer player = mock(ServerPlayer.class);
        ServerLevel level = mock(ServerLevel.class);
        when(player.level()).thenReturn(level);
        when(player.getId()).thenReturn(41);
        when(level.registryAccess()).thenReturn(RegistryAccess.EMPTY);
        var initial = new WarpData();
        initial.set(WarpType.TEMPORARY, temporary);
        initial.set(WarpType.NORMAL, normal);
        initial.set(WarpType.PERMANENT, permanent);
        initial.setCounter(counter);
        WarpData data = spy(initial);
        AttachmentFixture.attach(player, level::registryAccess).set(TTAttachments.WARP, data);
        RandomSource random = mock(RandomSource.class);
        when(player.getRandom()).thenReturn(random);
        when(player.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
        return new Fixture(player, data, random);
    }

    private static void assertSynchronizedCounter(Fixture fixture, MockedStatic<PayloadNetwork> packets, int expected) {
        var capture = ArgumentCaptor.forClass(AttachmentSyncPayload.class);
        packets.verify(() -> PayloadNetwork.sendToPlayer(eq(fixture.player()), capture.capture()));
        var payload = capture.getValue();
        assertEquals(fixture.player().getId(), payload.entityId());
        assertEquals(TTAttachments.WARP.id(), payload.key());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.data().orElseThrow()), RegistryAccess.EMPTY);
        try {
            assertEquals(expected, WarpData.STREAM_CODEC.decode(buffer).getCounter());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    private static MockedStatic<WarpManager> warpManager(Fixture fixture, int gear) {
        MockedStatic<WarpManager> warpManager = mockStatic(WarpManager.class);
        warpManager.when(() -> WarpManager.data(fixture.player())).thenReturn(fixture.data());
        warpManager.when(() -> WarpManager.getWarpFromGear(fixture.player())).thenReturn(gear);
        return warpManager;
    }

    private static MockedStatic<PlayerResearchAccess> researchAccess(Fixture fixture) {
        PlayerResearch research = mock(PlayerResearch.class);
        when(research.knows(any(ResourceLocation.class))).thenReturn(true);
        MockedStatic<PlayerResearchAccess> access = mockStatic(PlayerResearchAccess.class);
        access.when(() -> PlayerResearchAccess.of(fixture.player())).thenReturn(research);
        return access;
    }

    private record Fixture(ServerPlayer player, WarpData data, RandomSource random) {}
}
