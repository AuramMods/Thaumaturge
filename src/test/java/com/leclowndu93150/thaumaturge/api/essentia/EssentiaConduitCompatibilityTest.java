package com.leclowndu93150.thaumaturge.api.essentia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.blockentity.BlockCapabilityViews;
import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.util.LazyOptional;
import org.junit.jupiter.api.Test;

@SuppressWarnings("removal")
class EssentiaConduitCompatibilityTest {
    private final Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));

    @Test
    void absentPullHasNoAspectAndRejectsOffers() {
        EssentiaPull negative = new EssentiaPull(aspect, -4);
        assertEquals(EssentiaPull.NONE, negative);
        assertNull(negative.aspect());
        assertFalse(negative.isActive());
        assertFalse(negative.accepts(aspect));
        assertSame(EssentiaPull.NONE, EssentiaPull.of(aspect, 0));
        assertTrue(EssentiaPull.any(32).accepts(aspect));
        assertTrue(EssentiaPull.of(aspect, 64).accepts(aspect));
        assertFalse(EssentiaPull.of(aspect, 64).accepts(Holder.direct(mock(IAspect.class))));
    }

    @Test
    void legacyTransportDefaultsExposeConduitQueriesAndSimulation() {
        IEssentiaTransport transport = mock(IEssentiaTransport.class, CALLS_REAL_METHODS);
        when(transport.getEssentiaType(null)).thenReturn(aspect);
        when(transport.getEssentiaAmount(null)).thenReturn(5);
        when(transport.getSuctionType(null)).thenReturn(aspect);
        when(transport.getSuctionAmount(null)).thenReturn(64);
        when(transport.getMinimumSuction()).thenReturn(32);
        when(transport.isConnectable(Direction.UP)).thenReturn(true);
        when(transport.canInputFrom(Direction.UP)).thenReturn(true);
        when(transport.canOutputTo(Direction.UP)).thenReturn(true);
        when(transport.spaceFor(aspect, Direction.UP)).thenReturn(3);
        when(transport.getEssentiaType(Direction.UP)).thenReturn(aspect);
        when(transport.getEssentiaAmount(Direction.UP)).thenReturn(5);
        when(transport.addEssentia(aspect, 2, Direction.UP)).thenReturn(2);

        IEssentiaConduit conduit = transport;
        assertSame(aspect, conduit.heldAspect(null));
        assertEquals(5, conduit.heldAmount(null));
        assertEquals(EssentiaPull.of(aspect, 64), conduit.pull(null));
        assertEquals(32, conduit.pullThreshold());
        assertTrue(conduit.connectsOn(Direction.UP));
        assertTrue(conduit.receivesOn(Direction.UP));
        assertTrue(conduit.releasesOn(Direction.UP));
        assertEquals(3, conduit.roomFor(aspect, Direction.UP));
        assertEquals(3, conduit.insert(aspect, 10, Direction.UP, true));
        assertEquals(5, conduit.extract(aspect, 10, Direction.UP, true));
        verify(transport, never()).addEssentia(aspect, 10, Direction.UP);
        verify(transport, never()).takeEssentia(aspect, 10, Direction.UP);
        assertEquals(2, conduit.insert(aspect, 2, Direction.UP, false));
        verify(transport).addEssentia(aspect, 2, Direction.UP);
    }

    @Test
    void adapterKeepsExistingTransportAndNullIdentity() {
        IEssentiaTransport transport = mock(IEssentiaTransport.class);
        assertNull(ConduitAsTransport.view(null));
        assertSame(transport, ConduitAsTransport.view(transport));
    }

    @Test
    void adapterForwardsLegacyQueriesAndTransfersToSameConduit() {
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        when(conduit.heldAspect(null)).thenReturn(aspect);
        when(conduit.heldAmount(null)).thenReturn(8);
        when(conduit.pull(null)).thenReturn(EssentiaPull.of(aspect, 64));
        when(conduit.pullThreshold()).thenReturn(32);
        when(conduit.connectsOn(Direction.UP)).thenReturn(true);
        when(conduit.receivesOn(Direction.UP)).thenReturn(true);
        when(conduit.releasesOn(Direction.UP)).thenReturn(true);
        when(conduit.roomFor(aspect, Direction.UP)).thenReturn(7);
        when(conduit.insert(aspect, 3, Direction.UP, true)).thenReturn(3);
        when(conduit.insert(aspect, 3, Direction.UP, false)).thenReturn(3);
        when(conduit.extract(aspect, 2, Direction.UP, true)).thenReturn(2);
        when(conduit.extract(aspect, 2, Direction.UP, false)).thenReturn(2);
        IEssentiaTransport transport = ConduitAsTransport.view(conduit);

        assertSame(aspect, transport.getEssentiaType(null));
        assertEquals(8, transport.getEssentiaAmount(null));
        assertSame(aspect, transport.getSuctionType(null));
        assertEquals(64, transport.getSuctionAmount(null));
        assertEquals(32, transport.getMinimumSuction());
        assertTrue(transport.isConnectable(Direction.UP));
        assertTrue(transport.canInputFrom(Direction.UP));
        assertTrue(transport.canOutputTo(Direction.UP));
        assertEquals(7, transport.spaceFor(aspect, Direction.UP));
        assertEquals(3, transport.addEssentia(aspect, 3, Direction.UP, true));
        assertEquals(3, transport.addEssentia(aspect, 3, Direction.UP));
        assertEquals(2, transport.takeEssentia(aspect, 2, Direction.UP, true));
        assertEquals(2, transport.takeEssentia(aspect, 2, Direction.UP));
        clearInvocations(conduit);
        transport.setSuction(aspect, 128);
        verifyNoInteractions(conduit);
    }

    @Test
    void lookupPrefersConduitAndFallsBackToLegacyTransport() {
        Level level = mock(Level.class);
        BlockEntity provider = mock(BlockEntity.class);
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        IEssentiaTransport legacy = mock(IEssentiaTransport.class);
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(provider);
        when(provider.getCapability(eq(EssentiaCapabilities.CONDUIT), nullable(Direction.class)))
                .thenReturn(LazyOptional.empty());
        when(provider.getCapability(eq(EssentiaCapabilities.TRANSPORT), nullable(Direction.class)))
                .thenReturn(LazyOptional.empty());
        when(provider.getCapability(EssentiaCapabilities.CONDUIT, Direction.UP)).thenReturn(LazyOptional.of(() -> conduit));
        assertSame(conduit, EssentiaAccess.conduit(level, BlockPos.ZERO, Direction.UP));
        verify(provider, never()).getCapability(EssentiaCapabilities.TRANSPORT, Direction.UP);

        when(provider.getCapability(EssentiaCapabilities.TRANSPORT, null)).thenReturn(LazyOptional.of(() -> legacy));
        assertSame(legacy, EssentiaAccess.conduit(level, BlockPos.ZERO, null));
        assertFalse(EssentiaAccess.hasConduit(level, BlockPos.ZERO, Direction.DOWN));
    }

    @Test
    void blockEntityRegistrationPublishesConduitAndLegacyViewWithSameSides() {
        BlockCapabilityViews views = new BlockCapabilityViews();
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        EssentiaCapabilities.exposeConduit(views, side -> side == Direction.UP ? conduit : null);
        assertSame(conduit, views.getCapability(EssentiaCapabilities.CONDUIT, Direction.UP).orElseThrow(AssertionError::new));
        assertFalse(views.getCapability(EssentiaCapabilities.CONDUIT, Direction.DOWN).isPresent());
        assertTrue(views.getCapability(EssentiaCapabilities.TRANSPORT, Direction.UP).isPresent());
        assertFalse(views.getCapability(EssentiaCapabilities.TRANSPORT, Direction.DOWN).isPresent());
    }

    @Test
    void nativeBlockEntityPreservesNullableSideAndInvalidatesBothViews() {
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        class Endpoint extends RegistryBlockEntity {
            Endpoint() {
                super(BlockEntityType.FURNACE, BlockPos.ZERO, Blocks.FURNACE.defaultBlockState());
                exposeConduit(side -> side == null ? conduit : null);
            }
        }
        var endpoint = new Endpoint();
        var direct = endpoint.getCapability(EssentiaCapabilities.CONDUIT);
        var legacy = endpoint.getCapability(EssentiaCapabilities.TRANSPORT);
        assertSame(conduit, direct.orElseThrow(AssertionError::new));
        assertTrue(legacy.isPresent());
        assertFalse(endpoint.getCapability(EssentiaCapabilities.TRANSPORT, Direction.DOWN).isPresent());
        assertSame(legacy, endpoint.getCapability(EssentiaCapabilities.TRANSPORT));
        endpoint.setRemoved();
        assertFalse(direct.isPresent());
        assertFalse(legacy.isPresent());
        assertFalse(endpoint.getCapability(EssentiaCapabilities.CONDUIT).isPresent());
        endpoint.clearRemoved();
        assertSame(conduit, endpoint.getCapability(EssentiaCapabilities.CONDUIT).orElseThrow(AssertionError::new));
        assertNotSame(legacy, endpoint.getCapability(EssentiaCapabilities.TRANSPORT));
    }

    @Test
    void transferHonorsSimulationAndReturnsRefusedRemainderToSource() {
        IEssentiaConduit source = mock(IEssentiaConduit.class);
        IEssentiaConduit destination = mock(IEssentiaConduit.class);
        when(source.releasesOn(Direction.UP)).thenReturn(true);
        when(destination.receivesOn(Direction.DOWN)).thenReturn(true);
        when(source.extract(aspect, 10, Direction.UP, true)).thenReturn(7);
        when(destination.insert(aspect, 7, Direction.DOWN, true)).thenReturn(5);
        assertEquals(5, EssentiaTransfer.transfer(source, Direction.UP, destination, Direction.DOWN, aspect, 10, true));
        verify(source, never()).extract(any(), anyInt(), any(), eq(false));
        verify(destination, never()).insert(any(), anyInt(), any(), eq(false));

        when(source.extract(aspect, 5, Direction.UP, false)).thenReturn(5);
        when(destination.insert(aspect, 5, Direction.DOWN, false)).thenReturn(3);
        assertEquals(
                3, EssentiaTransfer.transfer(source, Direction.UP, destination, Direction.DOWN, aspect, 10, false));
        verify(source).insert(aspect, 2, Direction.UP, false);
    }

}
