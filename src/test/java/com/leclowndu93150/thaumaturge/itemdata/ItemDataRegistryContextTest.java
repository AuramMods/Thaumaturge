package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

/** Real holder/RegistryOps checks; these require the Forge-aware bootstrap used by the full suite. */
class ItemDataRegistryContextTest {
    static {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }
    private static final ResourceKey<Registry<String>> REGISTRY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("thaumaturge", "item_data_test"));
    private static final ResourceKey<String> VALUE =
            ResourceKey.create(REGISTRY, ResourceLocation.fromNamespaceAndPath("thaumaturge", "aer"));

    @Test
    void storedHolderIdsResolveAgainstEachWorldsLookup() {
        Fixture first = fixture();
        Fixture second = fixture();
        ItemDataKey<Holder<String>> key = new ItemDataRegistry("thaumaturge").<Holder<String>>registerComponentType(
                "aspect", b -> b.persistent(RegistryFixedCodec.create(REGISTRY))).get();
        CompoundTag values = new CompoundTag();
        CompoundTag defaults = new CompoundTag();
        ItemDataContext.withRegistries(first.provider(), () -> {
            ItemDataValues.set(values, defaults, key, first.holder(), ItemDataContext.ops());
            assertSame(first.holder(), ItemDataValues.get(values, defaults, key, ItemDataContext.ops()));
            return null;
        });
        assertEquals("thaumaturge:aer", values.getString(key.id().toString()));
        ItemDataContext.withRegistries(second.provider(), () -> {
            assertSame(second.holder(), ItemDataValues.get(values, defaults, key, ItemDataContext.ops()));
            assertThrows(IllegalArgumentException.class, () -> key.encode(ItemDataContext.ops(), first.holder()));
            return null;
        });
        assertThrows(IllegalArgumentException.class, () -> key.decode(NbtOps.INSTANCE, values.get(key.id().toString())));
    }

    @Test
    void nestedScopesRestoreRegistriesEvenAfterFailure() {
        Fixture outer = fixture();
        Fixture inner = fixture();
        ItemDataKey<Holder<String>> key = new ItemDataRegistry("thaumaturge").<Holder<String>>registerComponentType(
                "aspect", b -> b.persistent(RegistryFixedCodec.create(REGISTRY))).get();
        ItemDataContext.withRegistries(outer.provider(), () -> {
            assertSame(outer.holder(), key.decode(ItemDataContext.ops(), key.encode(ItemDataContext.ops(), outer.holder())));
            assertThrows(IllegalStateException.class, () -> ItemDataContext.withRegistries(inner.provider(), () -> {
                assertSame(inner.holder(), key.decode(ItemDataContext.ops(), key.encode(ItemDataContext.ops(), inner.holder())));
                throw new IllegalStateException("test");
            }));
            assertSame(outer.holder(), key.decode(ItemDataContext.ops(), key.encode(ItemDataContext.ops(), outer.holder())));
            return null;
        });
        assertThrows(NullPointerException.class, () -> ItemDataContext.withRegistries(null, () -> null));
    }

    private record Fixture(HolderLookup.Provider provider, Holder.Reference<String> holder) {}

    @SuppressWarnings("unchecked")
    private Fixture fixture() {
        HolderLookup.RegistryLookup<String> lookup = mock(HolderLookup.RegistryLookup.class);
        doReturn(REGISTRY).when(lookup).key();
        when(lookup.registryLifecycle()).thenReturn(Lifecycle.stable());
        when(lookup.canSerializeIn(lookup)).thenReturn(true);
        Holder.Reference<String> holder = Holder.Reference.createStandAlone(lookup, VALUE);
        holder.bindValue("air");
        when(lookup.get(VALUE)).thenReturn(Optional.of(holder));
        return new Fixture(HolderLookup.Provider.create(Stream.of(lookup)), holder);
    }
}
