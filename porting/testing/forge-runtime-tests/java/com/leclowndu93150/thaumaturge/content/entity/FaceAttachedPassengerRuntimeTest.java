package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class FaceAttachedPassengerRuntimeTest {
    @Test
    void nativeRideTickPlacesThePassengerAtThePlayersFaceAndTracksViewChanges() {
        var level = mock(Level.class);
        var player = new ProbePlayer(level);
        var rider = new ProbeRider(level);
        player.setPos(12.5, 64.2, -7.25);
        player.setYRot(90.0F);
        player.setXRot(20.0F);
        rider.setPos(0.0, 5.0, 0.0);
        player.positionRider(rider);
        assertEquals(new Vec3(0.0, 5.0, 0.0), rider.position());

        assertTrue(rider.startRiding(player, true));
        rider.setDeltaMovement(0.1, -0.2, 0.3);
        rider.rideTick();
        assertEquals(Vec3.ZERO, rider.getDeltaMovement());
        assertFacePosition(player, rider);
        var first = rider.position();

        player.setPos(18.0, 64.8, -1.0);
        player.setYRot(-25.0F);
        player.setXRot(-35.0F);
        rider.rideTick();
        assertFacePosition(player, rider);
        assertNotEquals(first, rider.position());
    }

    @Test
    void vehicleOverridesStillRunAndOrdinaryPassengersUseTheirNativeOffsets() {
        var level = mock(Level.class);
        var host = new ProbeHost(level);
        host.setPos(2.5, 70.0, 3.0);
        host.setBaby(true);
        var attached = new ProbeRider(level);
        var ordinary = new Spider(EntityType.SPIDER, level);
        assertTrue(attached.startRiding(host, true));
        assertTrue(ordinary.startRiding(host, true));

        host.positionRider(attached);
        assertFacePosition(host, attached);
        host.positionRider(ordinary);
        assertEquals(new Vec3(3.0, 72.0, 2.75), ordinary.position());
        assertEquals(2, host.specialPositionCalls);
    }

    @Test
    void aNonlivingVehicleStillUsesItsNativeVerticalSeat() {
        var level = mock(Level.class);
        var vehicle = new Snowball(EntityType.SNOWBALL, level);
        vehicle.setPos(7.0, 64.0, -3.0);
        var rider = new ProbeRider(level);
        assertTrue(rider.startRiding(vehicle, true));
        vehicle.positionRider(rider);
        double y = vehicle.getY() + vehicle.getPassengersRidingOffset() + rider.getMyRidingOffset();
        assertEquals(new Vec3(7.0, y, -3.0), rider.position());
    }

    private static void assertFacePosition(net.minecraft.world.entity.LivingEntity host, Entity passenger) {
        var center = passenger.position().add(0.0, passenger.getBbHeight() * 0.5, 0.0);
        var expected = host.getEyePosition().add(host.getLookAngle().scale(
                host.getBbWidth() / 2.0 + passenger.getBbWidth() / 4.0));
        assertEquals(expected.x, center.x, 1.0e-9);
        assertEquals(expected.y, center.y, 1.0e-9);
        assertEquals(expected.z, center.z, 1.0e-9);
    }

    private static final class ProbePlayer extends Player {
        ProbePlayer(Level level) { super(level, BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "PortLatch")); }

        @Override public boolean isCreative() { return false; }
        @Override public boolean isSpectator() { return false; }
    }

    private static final class ProbeRider extends Spider implements FaceAttachedPassenger {
        ProbeRider(Level level) { super(EntityType.SPIDER, level); }

        @Override public void tick() {}
    }

    private static final class ProbeHost extends Zombie {
        private int specialPositionCalls;

        ProbeHost(Level level) { super(EntityType.ZOMBIE, level); }

        @Override
        protected void positionRider(Entity passenger, MoveFunction move) {
            super.positionRider(passenger, move);
            if (hasPassenger(passenger)) {
                specialPositionCalls++;
                move.accept(passenger, getX() + 0.5, getY() + 2.0, getZ() - 0.25);
            }
        }
    }
}
