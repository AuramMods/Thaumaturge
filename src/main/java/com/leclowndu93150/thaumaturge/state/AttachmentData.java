package com.leclowndu93150.thaumaturge.state;

import com.leclowndu93150.thaumaturge.network.transport.PayloadNetwork;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Lifecycle, persistence and synchronization for the mod's Forge state capability. */
public final class AttachmentData {
    private static final ResourceLocation CAP_ID = new ResourceLocation("thaumaturge", "state");
    private static final Capability<AttachmentStorage> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private static volatile AttachmentRegistry schemas;

    private AttachmentData() {}

    public static void register(IEventBus modBus, AttachmentRegistry registry) {
        schemas = Objects.requireNonNull(registry);
        modBus.addListener(AttachmentData::registerCapability);
        modBus.addListener(AttachmentData::registerSync);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, AttachmentData::attachEntity);
        MinecraftForge.EVENT_BUS.addGenericListener(Level.class, AttachmentData::attachLevel);
        MinecraftForge.EVENT_BUS.addGenericListener(LevelChunk.class, AttachmentData::attachChunk);
        MinecraftForge.EVENT_BUS.addListener(AttachmentData::clonePlayer);
        MinecraftForge.EVENT_BUS.addListener(AttachmentData::login);
        MinecraftForge.EVENT_BUS.addListener(AttachmentData::respawn);
        MinecraftForge.EVENT_BUS.addListener(AttachmentData::dimensionChanged);
        MinecraftForge.EVENT_BUS.addListener(AttachmentData::startTracking);
    }

    static AttachmentRegistry registry() { return Objects.requireNonNull(schemas, "State schemas are not registered"); }

    public static AttachmentStorage storage(Object owner) {
        if (!(owner instanceof ICapabilityProvider provider)) {
            throw new IllegalArgumentException("Not a Forge capability owner: " + owner);
        }
        return provider.getCapability(CAPABILITY).orElseThrow(
                () -> new IllegalStateException("Thaumaturge state capability is missing on " + owner));
    }

    public static <T> T get(Object owner, AttachmentKey<T> key) { return storage(owner).get(key); }
    public static <T> T getExistingDataOrNull(Object owner, AttachmentKey<T> key) { return storage(owner).getExisting(key); }
    public static boolean has(Object owner, AttachmentKey<?> key) { return storage(owner).has(key); }

    public static <T> T set(Object owner, AttachmentKey<T> key, T value) {
        T previous = storage(owner).set(key, value);
        sync(owner, key);
        return previous;
    }

    public static <T> T remove(Object owner, AttachmentKey<T> key) {
        T previous = storage(owner).remove(key);
        sync(owner, key);
        return previous;
    }

    public static <T> void sync(Object owner, AttachmentKey<T> key) {
        if (key.syncCodec() == null || !(owner instanceof Entity entity) || entity.level().isClientSide) return;
        AttachmentSyncPayload payload = syncPayload(entity, key);
        if (key.ownerOnly()) {
            if (entity instanceof ServerPlayer player) PayloadNetwork.sendToPlayer(player, payload);
        } else {
            PayloadNetwork.sendToPlayersTrackingEntityAndSelf(entity, payload);
        }
    }

    private static void registerCapability(RegisterCapabilitiesEvent event) { event.register(AttachmentStorage.class); }

    private static void registerSync(FMLCommonSetupEvent event) {
        PayloadNetwork.registrar().playToClient(AttachmentSyncPayload.TYPE, AttachmentSyncPayload.STREAM_CODEC,
                (payload, context) -> ClientAttachmentState.apply(payload));
    }

    private static void attachEntity(AttachCapabilitiesEvent<Entity> event) {
        Entity owner = event.getObject();
        attach(event, () -> owner.level().registryAccess(), () -> {});
    }

    private static void attachLevel(AttachCapabilitiesEvent<Level> event) {
        attach(event, event.getObject()::registryAccess, () -> {});
    }

    static void attachChunk(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk owner = event.getObject();
        attach(event, () -> owner.getLevel().registryAccess(), () -> owner.setUnsaved(true));
    }

    private static void attach(AttachCapabilitiesEvent<?> event, Supplier<RegistryAccess> registries, Runnable changed) {
        var provider = new AttachmentCapabilityProvider(CAPABILITY,
                new AttachmentStorage(registry(), registries, changed));
        event.addCapability(CAP_ID, provider);
        event.addListener(provider::invalidate);
    }

    private static void clonePlayer(PlayerEvent.Clone event) {
        var original = event.getOriginal();
        original.reviveCaps();
        try {
            storage(original).copyPersistentTo(storage(event.getEntity()), event.isWasDeath());
        } finally {
            original.invalidateCaps();
        }
    }

    private static void login(PlayerEvent.PlayerLoggedInEvent event) { syncSelf(event); }
    private static void respawn(PlayerEvent.PlayerRespawnEvent event) { syncSelf(event); }
    private static void dimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) { syncSelf(event); }

    private static void syncSelf(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncPresent(player, player);
    }

    private static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) syncPresent(event.getTarget(), player);
    }

    private static void syncPresent(Entity owner, ServerPlayer recipient) {
        for (AttachmentKey<?> key : storage(owner).presentKeys()) {
            if (key.syncCodec() != null && (!key.ownerOnly() || owner == recipient)) {
                PayloadNetwork.sendToPlayer(recipient, syncPayload(owner, key));
            }
        }
    }

    private static <T> AttachmentSyncPayload syncPayload(Entity owner, AttachmentKey<T> key) {
        T value = storage(owner).getExisting(key);
        if (value == null) return new AttachmentSyncPayload(owner.getId(), key.id(), Optional.empty());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), owner.level().registryAccess());
        try {
            key.syncCodec().encode(buffer, value);
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return new AttachmentSyncPayload(owner.getId(), key.id(), Optional.of(bytes));
        } finally {
            buffer.release();
        }
    }

}
