package com.leclowndu93150.thaumaturge.serialization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.itemdata.ItemDataContext;
import com.mojang.serialization.Codec;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.junit.jupiter.api.Test;

class RegistryContextTest {
    @Test
    void explicitScopesOverrideAnAttachedWorldAndRestoreNestedScopes() {
        var world = mock(HolderLookup.Provider.class);
        var outer = mock(HolderLookup.Provider.class);
        var inner = mock(HolderLookup.Provider.class);
        assertSame(world, RegistryContext.resolve(world));
        RegistryContext.withRegistries(outer, () -> {
            assertSame(outer, RegistryContext.resolve(world));
            RegistryContext.withRegistries(inner, () -> {
                assertSame(inner, RegistryContext.resolve(world));
                return null;
            });
            assertSame(outer, RegistryContext.resolve(world));
            return null;
        });
        assertNull(RegistryContext.scoped());
        assertSame(world, RegistryContext.resolve(world));
    }

    @Test
    void exceptionsAndNullLookupsDoNotLeakOrReplaceTheCurrentScope() {
        var outer = mock(HolderLookup.Provider.class);
        var inner = mock(HolderLookup.Provider.class);
        RegistryContext.withRegistries(outer, () -> {
            assertThrows(IllegalStateException.class, () -> RegistryContext.withRegistries(inner, () -> {
                throw new IllegalStateException("codec failed");
            }));
            assertSame(outer, RegistryContext.scoped());
            assertThrows(NullPointerException.class, () -> RegistryContext.withRegistries(null, () -> null));
            assertSame(outer, RegistryContext.scoped());
            return null;
        });
        assertNull(RegistryContext.scoped());
    }

    @Test
    void itemAndBlockCodecsShareTheSameRegistryScope() {
        var registries = mock(HolderLookup.Provider.class);
        ItemDataContext.withRegistries(registries, () -> {
            assertSame(registries, RegistryContext.scoped());
            assertInstanceOf(RegistryOps.class, RegistryContext.ops());
            var tag = Codec.INT.encodeStart(ItemDataContext.ops(), 9).result().orElseThrow();
            assertEquals(9, Codec.INT.parse(RegistryContext.ops(), tag).result().orElseThrow());
            return null;
        });
        RegistryContext.withRegistries(registries, () -> {
            assertInstanceOf(RegistryOps.class, ItemDataContext.ops());
            return null;
        });
        assertNull(RegistryContext.scoped());
    }

    @Test
    void scopesAreNotInheritedByOtherThreads() throws Exception {
        var registries = mock(HolderLookup.Provider.class);
        AtomicReference<HolderLookup.Provider> observed = new AtomicReference<>(registries);
        RegistryContext.withRegistries(registries, () -> {
            Thread worker = new Thread(() -> observed.set(RegistryContext.scoped()), "registry-context-test");
            worker.start();
            try { worker.join(5000); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new AssertionError(ex); }
            assertFalse(worker.isAlive());
            assertNull(observed.get());
            assertSame(registries, RegistryContext.scoped());
            return null;
        });
        assertNull(RegistryContext.scoped());
    }
}
