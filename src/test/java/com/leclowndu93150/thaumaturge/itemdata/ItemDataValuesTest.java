package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class ItemDataValuesTest {
    private final ItemDataRegistry registry = new ItemDataRegistry("thaumaturge");
    private final ItemDataKey<Integer> charge = registry.<Integer>registerComponentType("charge",
            b -> b.persistent(Codec.intRange(0, 100))).get();
    private final CompoundTag defaults = new CompoundTag();

    @Test
    void absentKeysStayAbsentUntilSet() {
        assertNull(get(null));
        assertFalse(ItemDataValues.has(null, defaults, charge));
        CompoundTag values = new CompoundTag();
        ItemDataValues.set(values, defaults, charge, 27, NbtOps.INSTANCE);
        assertTrue(ItemDataValues.has(values, defaults, charge));
        assertEquals(27, get(values));
        ItemDataValues.remove(values, defaults, charge);
        assertNull(get(values));
        assertTrue(values.isEmpty());
    }

    @Test
    void defaultsCanBeOverriddenRemovedAndRestoredCanonically() {
        defaults.putInt(charge.id().toString(), 20);
        CompoundTag values = new CompoundTag();
        assertEquals(20, get(values));
        ItemDataValues.set(values, defaults, charge, 30, NbtOps.INSTANCE);
        assertEquals(30, get(values));
        assertEquals(20, defaults.getInt(charge.id().toString()));
        ItemDataValues.remove(values, defaults, charge);
        assertNull(get(values));
        assertFalse(ItemDataValues.has(values, defaults, charge));
        ItemDataValues.set(values, defaults, charge, 20, NbtOps.INSTANCE);
        assertEquals(20, get(values));
        assertTrue(values.isEmpty(), "Setting the default must restore fresh-stack NBT identity");
    }

    @Test
    void valuesAndExplicitDefaultRemovalSurviveActualNbtIo() throws Exception {
        defaults.putInt(charge.id().toString(), 20);
        ItemDataKey<String> name = registry.<String>registerComponentType("label", b -> b.persistent(Codec.STRING)).get();
        CompoundTag values = new CompoundTag();
        ItemDataValues.set(values, defaults, name, "Aér ✨", NbtOps.INSTANCE);
        ItemDataValues.remove(values, defaults, charge);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(values, bytes);
        CompoundTag loaded = NbtIo.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
        assertEquals("Aér ✨", ItemDataValues.get(loaded, defaults, name, NbtOps.INSTANCE));
        assertNull(get(loaded));
        assertFalse(ItemDataValues.has(loaded, defaults, charge));
    }

    @Test
    void stackCopiesAndUnknownFieldsStayIndependentAndPreserved() {
        CompoundTag values = new CompoundTag();
        values.putString("addon:future", "keep");
        ItemDataValues.set(values, defaults, charge, 5, NbtOps.INSTANCE);
        CompoundTag copy = values.copy();
        ItemDataValues.set(copy, defaults, charge, 8, NbtOps.INSTANCE);
        assertEquals(5, get(values));
        assertEquals(8, get(copy));
        assertEquals("keep", copy.getString("addon:future"));
        ItemDataValues.remove(copy, defaults, charge);
        assertEquals("keep", copy.getString("addon:future"));
    }

    @Test
    void mutableCodecValuesDoNotAliasInputsStoredDataOrDefaults() {
        Codec<List<Integer>> codec = Codec.INT.listOf().xmap(ArrayList::new, List::copyOf);
        ItemDataKey<List<Integer>> key = registry.<List<Integer>>registerComponentType("list", b -> b.persistent(codec)).get();
        ArrayList<Integer> source = new ArrayList<>(List.of(1, 2));
        CompoundTag values = new CompoundTag();
        ItemDataValues.set(values, defaults, key, source, NbtOps.INSTANCE);
        source.add(3);
        List<Integer> first = ItemDataValues.get(values, defaults, key, NbtOps.INSTANCE);
        assertEquals(List.of(1, 2), first);
        first.clear();
        assertEquals(List.of(1, 2), ItemDataValues.get(values, defaults, key, NbtOps.INSTANCE));
        defaults.put(key.id().toString(), key.encode(NbtOps.INSTANCE, List.of(4)));
        ItemDataValues.get(null, defaults, key, NbtOps.INSTANCE).clear();
        assertEquals(List.of(4), ItemDataValues.get(null, defaults, key, NbtOps.INSTANCE));
    }

    @Test
    void invalidWritesFailBeforeChangingThePreviousValueOrMarker() {
        CompoundTag values = new CompoundTag();
        ItemDataValues.set(values, defaults, charge, 100, NbtOps.INSTANCE);
        assertThrows(IllegalArgumentException.class, () -> ItemDataValues.set(values, defaults, charge, 101, NbtOps.INSTANCE));
        assertEquals(100, get(values));
        defaults.putInt(charge.id().toString(), 20);
        ItemDataValues.remove(values, defaults, charge);
        CompoundTag before = values.copy();
        assertThrows(IllegalArgumentException.class, () -> ItemDataValues.set(values, defaults, charge, -1, NbtOps.INSTANCE));
        assertEquals(before, values);
        assertThrows(NullPointerException.class, () -> ItemDataValues.set(values, defaults, charge, null, NbtOps.INSTANCE));
    }

    @Test
    void malformedValuesAndRemovalMarkersReportErrors() {
        CompoundTag values = new CompoundTag();
        values.putString(charge.id().toString(), "wrong");
        assertThrows(IllegalArgumentException.class, () -> get(values));
        values.remove(charge.id().toString());
        values.putString("!" + charge.id(), "wrong");
        assertThrows(IllegalArgumentException.class, () -> get(values));
        assertThrows(IllegalArgumentException.class, () -> ItemDataValues.has(values, defaults, charge));
        values.putBoolean("!" + charge.id(), false);
        assertThrows(IllegalArgumentException.class, () -> get(values));
        values.putBoolean("!" + charge.id(), true);
        values.putInt(charge.id().toString(), 1);
        assertThrows(IllegalArgumentException.class, () -> get(values));
    }

    @Test
    void catalogRetainsStableIdsAndLazilyConstructsEachSchemaOnce() {
        AtomicInteger calls = new AtomicInteger();
        var lazy = registry.<String>registerComponentType("lazy", b -> {
            calls.incrementAndGet();
            return b.persistent(Codec.STRING);
        });
        assertEquals(0, calls.get());
        assertTrue(registry.ids().contains(charge.id()));
        assertSame(lazy.get(), registry.get(lazy.get().id()));
        assertEquals(1, calls.get());
        assertThrows(IllegalArgumentException.class, () -> registry.registerComponentType("charge", b -> b));
        assertThrows(NullPointerException.class, () -> registry.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("addon", "missing")));
        assertThrows(NullPointerException.class, () -> registry.registerComponentType("no_codec", b -> b).get());
    }

    private Integer get(CompoundTag values) {
        return ItemDataValues.get(values, defaults, charge, NbtOps.INSTANCE);
    }
}
