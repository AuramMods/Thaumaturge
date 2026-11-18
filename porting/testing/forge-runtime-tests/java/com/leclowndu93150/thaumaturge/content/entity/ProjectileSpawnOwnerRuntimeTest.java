package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeDeferredRegistriesSetup;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProjectileSpawnOwnerRuntimeTest {
    @BeforeAll
    static void bindForgeFluidTypesBeforeConstructingEntities() throws Exception {
        var bus = BusBuilder.builder().build();
        ForgeDeferredRegistriesSetup.setup(bus);
        var vanillaFluids = ForgeMod.class.getDeclaredField("VANILLA_FLUID_TYPES");
        vanillaFluids.setAccessible(true);
        ((DeferredRegister<?>) vanillaFluids.get(null)).register(bus);

        var created = new NewRegistryEvent();
        bus.post(created);
        var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
        fill.setAccessible(true);
        fill.invoke(created);

        var registry = (ForgeRegistry<?>) ForgeRegistries.FLUID_TYPES.get();
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        bus.post(ctor.newInstance(ForgeRegistries.Keys.FLUID_TYPES, registry, null));
        registry.freeze();
    }

    @Test
    void forgeSpawnPacketCarriesProjectileOwnerAndFollowingFields() {
        var source = new SpawnProbe(mock(Level.class));
        var ownerId = UUID.randomUUID();
        var owner = mock(Entity.class);
        when(owner.getId()).thenReturn(257);
        when(owner.getUUID()).thenReturn(ownerId);
        source.setOwner(owner);
        source.setPos(2.25, 8.0, -15.5);
        source.color = 0xFF5082B4;

        var remoteOwner = mock(Entity.class);
        when(remoteOwner.getUUID()).thenReturn(ownerId);
        var receivingLevel = mock(Level.class);
        when(receivingLevel.getEntity(257)).thenReturn(remoteOwner);
        var receiver = new SpawnProbe(receivingLevel);

        var packet = assertInstanceOf(ClientboundCustomPayloadPacket.class, source.getAddEntityPacket());
        FriendlyByteBuf wire = packet.getData();
        try {
            assertEquals(0, wire.readVarInt()); // Forge's SpawnEntity discriminator.
            var spawn = PlayMessages.SpawnEntity.decode(wire);
            assertFalse(wire.isReadable());
            var data = spawn.getAdditionalData();
            try {
                assertEquals(BuiltInRegistries.ENTITY_TYPE.getId(EntityType.SNOWBALL), spawn.getTypeId());
                assertEquals(source.getId(), spawn.getEntityId());
                assertEquals(source.getUUID(), spawn.getUuid());
                assertEquals(source.getZ(), spawn.getPosZ());
                receiver.readSpawnData(data);
                assertFalse(data.isReadable());
                assertSame(remoteOwner, receiver.getOwner());
                assertEquals(source.color, receiver.color);
            } finally {
                data.release();
            }
        } finally {
            wire.release();
        }
    }

    @Test
    void missingAndUntrackedOwnersLeaveTheProjectileUnowned() {
        var level = mock(Level.class);
        var receiver = new SpawnProbe(level);
        var data = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var source = new SpawnProbe(mock(Level.class));
            source.color = 0x10203040;
            source.writeSpawnData(data);
            receiver.readSpawnData(data);
            assertNull(receiver.getOwner());
            assertEquals(source.color, receiver.color);
            verify(level, never()).getEntity(0);

            data.clear();
            data.writeVarInt(193);
            data.writeInt(0xFF0000FF);
            receiver.readSpawnData(data);
            assertNull(receiver.getOwner());
            assertEquals(0xFF0000FF, receiver.color);
            verify(level).getEntity(193);
        } finally {
            data.release();
        }
    }

    private static final class SpawnProbe extends ThrowableProjectile implements IEntityAdditionalSpawnData {
        private int color;

        private SpawnProbe(Level level) {
            super(EntityType.SNOWBALL, level);
        }

        @Override
        protected void defineSynchedData() {}

        @Override
        public Packet<ClientGamePacketListener> getAddEntityPacket() {
            return NetworkHooks.getEntitySpawningPacket(this);
        }

        @Override
        public void writeSpawnData(FriendlyByteBuf data) {
            ProjectileSpawnOwner.write(data, this);
            data.writeInt(color);
        }

        @Override
        public void readSpawnData(FriendlyByteBuf data) {
            ProjectileSpawnOwner.read(data, this);
            color = data.readInt();
        }
    }
}
