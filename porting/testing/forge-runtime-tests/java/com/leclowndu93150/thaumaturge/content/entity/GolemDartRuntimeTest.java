package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GolemDartRuntimeTest {
    @Test
    void dartLaunchesFromTheShooterAndPairsThroughVanillaSpawnAndMotionPackets() {
        var type = registerFixtureType();
        ServerLevel server = mock(ServerLevel.class);
        Zombie shooter = new Zombie(EntityType.ZOMBIE, server);
        shooter.setPos(5.0, 12.0, -3.0);
        Zombie target = new Zombie(EntityType.ZOMBIE, server);
        target.setPos(9.0, 21.0, 7.0);
        double damage = 3.7216666666667;
        double loft = 0.25;
        float speed = 1.6F;

        EntityGolemDart.loose(type, shooter, target, damage, loft, speed, 0.0F);

        var spawned = ArgumentCaptor.forClass(Entity.class);
        verify(server).addFreshEntity(spawned.capture());
        var dart = assertInstanceOf(EntityGolemDart.class, spawned.getValue());
        var origin = new Vec3(shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ());
        var velocity = new Vec3(target.getX(), target.getEyeY() + loft, target.getZ())
                .subtract(origin).normalize().scale(speed);
        assertSame(type, dart.getType());
        assertEquals(origin, dart.position());
        assertEquals(velocity, dart.getDeltaMovement());
        assertEquals(damage, dart.getBaseDamage());
        assertSame(shooter, dart.getOwner());
        assertEquals(AbstractArrow.Pickup.DISALLOWED, dart.pickup);
        assertSame(Items.ARROW, dart.getPickupItem().getItem());

        var packets = new ArrayList<Packet<?>>();
        new ServerEntity(server, dart, type.updateInterval(), type.trackDeltas(), packets::add)
                .sendPairingData(mock(ServerPlayer.class), packets::add);
        var sentSpawn = assertInstanceOf(ClientboundAddEntityPacket.class, packets.get(0));
        ClientboundAddEntityPacket spawn;
        var wire = new FriendlyByteBuf(Unpooled.buffer());
        try {
            sentSpawn.write(wire);
            spawn = new ClientboundAddEntityPacket(wire);
            assertFalse(wire.isReadable());
        } finally {
            wire.release();
        }
        assertSame(type, spawn.getType());
        assertEquals(shooter.getId(), spawn.getData());
        var motion = packets.stream().filter(ClientboundSetEntityMotionPacket.class::isInstance)
                .map(ClientboundSetEntityMotionPacket.class::cast).findFirst().orElseThrow();
        assertEquals(dart.getId(), motion.getId());

        Level client = mock(Level.class);
        when(client.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);
        Zombie clientShooter = new Zombie(EntityType.ZOMBIE, client);
        when(client.getEntity(shooter.getId())).thenReturn(clientShooter);
        EntityGolemDart received = type.create(client);
        received.recreateFromPacket(spawn);
        received.lerpMotion(motion.getXa() / 8000.0, motion.getYa() / 8000.0, motion.getZa() / 8000.0);
        assertSame(clientShooter, received.getOwner());
        assertEquals(origin, received.position());
        assertEquals(dart.getId(), received.getId());
        assertEquals(dart.getUUID(), received.getUUID());
        assertEquals(0.0, received.getDeltaMovement().distanceTo(velocity), Math.sqrt(3.0) / 8000.0);
    }

    private static EntityType<EntityGolemDart> registerFixtureType() {
        var id = new ResourceLocation("port_golem_dart", "dart");
        var forge = (ForgeRegistry<?>) ForgeRegistries.ENTITY_TYPES;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.ENTITY_TYPE;
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            var type = EntityType.Builder.of(EntityGolemDart::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F).clientTrackingRange(4).updateInterval(20).build(id.toString());
            ForgeRegistries.ENTITY_TYPES.register(id, type);
            return type;
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }
}
