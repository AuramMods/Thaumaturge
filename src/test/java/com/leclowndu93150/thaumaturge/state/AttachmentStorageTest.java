package com.leclowndu93150.thaumaturge.state;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class AttachmentStorageTest {
    private final AttachmentRegistry keys = new AttachmentRegistry("thaumaturge_test");
    private final AtomicInteger changes = new AtomicInteger();

    @Test
    void defaultsAreLazyStablePerOwnerAndNeverSharedAcrossOwners() {
        AtomicInteger creations = new AtomicInteger();
        var key = keys.register("items", AttachmentKey.builder(() -> {
            creations.incrementAndGet(); return new ArrayList<Integer>();
        }));
        var first = storage(); var second = storage();
        assertNull(first.getExisting(key));
        assertFalse(first.has(key));
        assertEquals(0, creations.get());
        first.get(key).add(1);
        assertSame(first.get(key), first.get(key));
        assertTrue(first.has(key));
        assertEquals(List.of(1), first.get(key));
        assertEquals(List.of(), second.get(key));
        assertEquals(2, creations.get());
        assertEquals(0, changes.get(), "transient defaults do not dirty persistent state");
    }

    @Test
    void persistentValuesSurviveSaveAndLazyReload() {
        var key = keys.register("count", AttachmentKey.builder(() -> 0).serialize(Codec.INT));
        var source = storage();
        assertEquals(0, source.get(key));
        assertEquals(1, changes.get());
        source.set(key, 42);
        CompoundTag saved = source.save();
        assertEquals(42, saved.getInt(key.id().toString()));
        var loaded = storage(); loaded.load(saved);
        assertTrue(loaded.has(key));
        assertEquals(42, loaded.get(key));
        assertEquals(2, changes.get(), "load/decoding do not mark unchanged state dirty");
    }

    @Test
    void transientStateIsNotSavedOrCopiedEvenIfAccessed() {
        var key = keys.register("queue", AttachmentKey.builder(ArrayList<Integer>::new));
        var source = storage(); source.get(key).add(9);
        assertFalse(source.save().contains(key.id().toString()));
        var loaded = storage(); loaded.load(source.save());
        assertFalse(loaded.has(key));
        var clone = storage(); source.copyPersistentTo(clone, false);
        assertFalse(clone.has(key));
        assertTrue(clone.get(key).isEmpty());
    }

    @Test
    void conditionalPersistenceOmitsInactiveValuesAndRemovesOldSavedValues() {
        var key = keys.register("hover", AttachmentKey.builder(() -> false).serialize(Codec.BOOL, value -> value));
        var source = storage(); source.set(key, true);
        var loaded = storage(); loaded.load(source.save());
        assertTrue(loaded.get(key));
        loaded.set(key, false);
        assertFalse(loaded.save().contains(key.id().toString()));
    }

    @Test
    void deathCopiesOnlyPersistentKeysWithTheDeathFlag() {
        var kept = keys.register("knowledge", AttachmentKey.builder(() -> 0).serialize(Codec.INT).copyOnDeath());
        var reset = keys.register("hover", AttachmentKey.builder(() -> false).serialize(Codec.BOOL));
        var temporary = keys.register("temporary", AttachmentKey.builder(() -> 0).copyOnDeath());
        var source = storage(); source.set(kept, 25); source.set(reset, true); source.set(temporary, 11);
        var target = storage(); source.copyPersistentTo(target, true);
        assertEquals(25, target.get(kept));
        assertFalse(target.has(reset));
        assertFalse(target.get(reset));
        assertFalse(target.has(temporary));
    }

    @Test
    void nonDeathCloneCopiesAllPersistentDataWithoutAliasingMutableValues() {
        var key = keys.register("notes", AttachmentKey.builder(ArrayList<Integer>::new)
                .serialize(Codec.INT.listOf().xmap(ArrayList::new, List::copyOf)));
        var source = storage(); source.get(key).add(1);
        var target = storage(); source.copyPersistentTo(target, false);
        assertEquals(List.of(1), target.get(key));
        assertNotSame(source.get(key), target.get(key));
        source.get(key).add(2);
        assertEquals(List.of(1), target.get(key));
    }

    @Test
    void removingStateReturnsTheOldValueAndUpdatesPersistence() {
        var key = keys.register("count", AttachmentKey.builder(() -> 0).serialize(Codec.INT));
        var source = storage(); source.set(key, 7);
        assertEquals(7, source.remove(key));
        assertFalse(source.has(key));
        assertFalse(source.save().contains(key.id().toString()));
        int before = changes.get();
        assertNull(source.remove(key));
        assertEquals(before, changes.get());
        assertEquals(0, source.get(key));
    }

    @Test
    void unknownDataRoundTripsAndInputOutputTagsDoNotAliasTheOwner() {
        CompoundTag raw = new CompoundTag(); raw.putString("addon:future", "preserve");
        var source = storage(); source.load(raw);
        raw.putString("addon:future", "changed input");
        CompoundTag saved = source.save();
        assertEquals("preserve", saved.getString("addon:future"));
        saved.putString("addon:future", "changed output");
        assertEquals("preserve", source.save().getString("addon:future"));
    }

    @Test
    void loadingReplacesPreviousRuntimeValues() {
        var key = keys.register("count", AttachmentKey.builder(() -> 0).serialize(Codec.INT));
        var source = storage(); source.set(key, 7);
        CompoundTag raw = new CompoundTag(); raw.putInt(key.id().toString(), 2);
        source.load(raw);
        assertEquals(2, source.get(key));
    }

    @Test
    void corruptPersistentDataFailsWhenReadAndRemainsAvailableForRecovery() {
        var key = keys.register("count", AttachmentKey.builder(() -> 0).serialize(Codec.INT));
        CompoundTag raw = new CompoundTag(); raw.putString(key.id().toString(), "invalid integer");
        var source = storage(); source.load(raw);
        assertTrue(source.has(key));
        var error = assertThrows(IllegalStateException.class, () -> source.get(key));
        assertTrue(error.getMessage().contains(key.id().toString()));
        assertEquals(raw, source.save());
    }

    @Test
    void invalidEncodeAndNullDefaultsFailExplicitly() {
        var key = keys.register("bounded", AttachmentKey.builder(() -> 1).serialize(Codec.intRange(1, 5)));
        var source = storage(); source.set(key, 0);
        assertThrows(IllegalStateException.class, source::save);
        var absent = keys.register("null", AttachmentKey.builder(() -> (String) null));
        assertThrows(NullPointerException.class, () -> source.get(absent));
        assertThrows(NullPointerException.class, () -> source.set(key, null));
    }

    @Test
    void syncPoliciesDistinguishOwnerOnlyAndTrackingValues() {
        var owner = keys.register("knowledge", AttachmentKey.builder(() -> 0).serialize(Codec.INT)
                .syncToOwner(ByteBufCodecs.VAR_INT).copyOnDeath());
        var tracked = keys.register("cape", AttachmentKey.builder(() -> false).sync(ByteBufCodecs.BOOL));
        assertTrue(owner.ownerOnly()); assertTrue(owner.copyOnDeath()); assertNotNull(owner.syncCodec());
        assertFalse(tracked.ownerOnly()); assertFalse(tracked.persistent()); assertNotNull(tracked.syncCodec());
    }

    @Test
    void duplicateKeysDoNotReplaceTheOriginalSchema() {
        var key = keys.register("count", AttachmentKey.builder(() -> 0));
        assertThrows(IllegalStateException.class, () -> keys.register("count", AttachmentKey.builder(() -> 99)));
        assertSame(key, keys.get(key.id()));
        assertEquals(List.of(key), List.copyOf(keys.keys()));
    }

    @Test
    void syncPayloadCarriesValuesAndExplicitRemovals() {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        ResourceLocation id = new ResourceLocation("thaumaturge", "knowledge");
        try {
            AttachmentSyncPayload.STREAM_CODEC.encode(buffer, new AttachmentSyncPayload(7, id, Optional.of(new byte[] {1, 2, 3})));
            var decoded = AttachmentSyncPayload.STREAM_CODEC.decode(buffer);
            assertEquals(7, decoded.entityId()); assertEquals(id, decoded.key());
            assertArrayEquals(new byte[] {1, 2, 3}, decoded.data().orElseThrow());
            assertFalse(buffer.isReadable());
            AttachmentSyncPayload.STREAM_CODEC.encode(buffer, new AttachmentSyncPayload(7, id, Optional.empty()));
            assertTrue(AttachmentSyncPayload.STREAM_CODEC.decode(buffer).data().isEmpty());
        } finally { buffer.release(); }
    }

    private AttachmentStorage storage() {
        return new AttachmentStorage(keys, () -> RegistryAccess.EMPTY, changes::incrementAndGet);
    }
}
