package com.leclowndu93150.thaumaturge.state;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import org.junit.jupiter.api.Test;

class AttachmentCapabilityProviderTest {
    // Token resolution is a Forge-coremod concern. Identity keys isolate the
    // provider's actual LazyOptional lifecycle and NBT behavior in this unit test.
    @SuppressWarnings("unchecked")
    private static final Capability<AttachmentStorage> STATE = mock(Capability.class);

    @Test
    void invalidatedHandlesStayInvalidAndARevivedHostCanObtainTheSameStorage() {
        var keys = new AttachmentRegistry("thaumaturge_test");
        var key = keys.register("knowledge", AttachmentKey.builder(() -> 0).serialize(Codec.INT).copyOnDeath());
        var storage = new AttachmentStorage(keys, () -> RegistryAccess.EMPTY, () -> {});
        storage.set(key, 17);
        var provider = new AttachmentCapabilityProvider(STATE, storage);
        var old = provider.getCapability(STATE, null);
        assertSame(storage, old.orElseThrow(AssertionError::new));
        provider.invalidate();
        assertFalse(old.isPresent());
        var revived = provider.getCapability(STATE, null);
        assertNotSame(old, revived);
        assertSame(storage, revived.orElseThrow(AssertionError::new));
        assertEquals(17, revived.orElseThrow(AssertionError::new).get(key));
        provider.invalidate();
        assertFalse(revived.isPresent());
        assertFalse(old.isPresent());
    }

    @Test
    void nativeProviderSerializationRoundTripsStateAndRejectsOtherCapabilities() {
        var keys = new AttachmentRegistry("thaumaturge_test");
        var key = keys.register("knowledge", AttachmentKey.builder(() -> 0).serialize(Codec.INT));
        var first = new AttachmentCapabilityProvider(STATE, new AttachmentStorage(keys, () -> RegistryAccess.EMPTY, () -> {}));
        first.getCapability(STATE, null).orElseThrow(AssertionError::new).set(key, 42);
        CompoundTag saved = first.serializeNBT();
        var loaded = new AttachmentCapabilityProvider(STATE, new AttachmentStorage(keys, () -> RegistryAccess.EMPTY, () -> {}));
        loaded.deserializeNBT(saved);
        assertEquals(42, loaded.getCapability(STATE, null).orElseThrow(AssertionError::new).get(key));
        @SuppressWarnings("unchecked")
        Capability<String> other = mock(Capability.class);
        assertFalse(loaded.getCapability(other, null).isPresent());
    }
}
