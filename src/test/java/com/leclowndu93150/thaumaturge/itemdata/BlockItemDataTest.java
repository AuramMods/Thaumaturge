package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

/** Contract and serialization checks; actual block entities still require the Forge runtime. */
class BlockItemDataTest {
    private final ItemDataRegistry registry = new ItemDataRegistry("thaumaturge");
    private final ItemDataKey<Integer> amount = registry.<Integer>registerComponentType("amount", b -> b.persistent(Codec.INT)).get();
    private final ItemDataKey<String> label = registry.<String>registerComponentType("label", b -> b.persistent(Codec.STRING)).get();

    private final class TestBlock implements BlockItemData {
        int stored;
        String name;
        TestBlock(int stored, String name) { this.stored = stored; this.name = name; }
        public void collectItemData(ItemDataPatch.Builder output) {
            output.set(amount, stored);
            if (name != null) output.set(label, name);
        }
        public void applyItemData(ItemDataReader input) {
            Integer value = input.get(amount);
            if (value != null) stored = value;
            name = input.get(label);
        }
    }

    @Test
    void collectedFieldsRoundTripThroughActualNbtAndTheReadOnlyInput() {
        TestBlock source = new TestBlock(64, "aer");
        var codec = ItemDataPatch.codec(registry::get);
        var nbt = codec.encodeStart(NbtOps.INSTANCE, source.collectItemData()).result().orElseThrow();
        ItemDataPatch decoded = codec.parse(NbtOps.INSTANCE, nbt).result().orElseThrow();
        TestBlock placed = new TestBlock(0, null);
        placed.applyItemData(ItemDataReader.fromPatch(decoded));
        assertEquals(64, placed.stored);
        assertEquals("aer", placed.name);
        source.stored = 1;
        assertEquals(64, placed.stored);
    }

    @Test
    void lootSelectionCopiesOnlySelectedFieldsWithoutChangingTheSource() {
        TestBlock source = new TestBlock(40, "ignis");
        ItemDataPatch selected = source.collectItemData().select(new ItemDataSelection(Optional.of(Set.of(label.id())), Set.of()));
        assertNull(selected.get(amount));
        assertEquals("ignis", selected.get(label));
        assertEquals(40, source.stored);
        TestBlock placed = new TestBlock(0, null);
        placed.applyItemData(ItemDataReader.fromPatch(selected));
        assertEquals(0, placed.stored);
        assertEquals("ignis", placed.name);
    }

    @Test
    void emptyAndRemovedInputsStayAbsent() {
        ItemDataReader input = ItemDataReader.fromPatch(ItemDataPatch.builder().remove(label).build());
        assertNull(input.get(amount));
        assertNull(input.get(label));
        assertNull(ItemDataReader.fromPatch(ItemDataPatch.EMPTY).get(amount));
        assertTrue(new TestBlock(5, "terra").collectItemData()
                .select(new ItemDataSelection(Optional.of(Set.of()), Set.of())).isEmpty());
    }
}
