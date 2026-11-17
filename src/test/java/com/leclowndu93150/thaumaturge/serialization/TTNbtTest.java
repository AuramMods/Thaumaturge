package com.leclowndu93150.thaumaturge.serialization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class TTNbtTest {
    @Test
    void scalarAndNestedValuesRoundTripWithAndWithoutRegistryOps() {
        CompoundTag tag = new CompoundTag();
        var registries = mock(HolderLookup.Provider.class);
        TTNbt.store(tag, "count", Codec.INT, registries, 14);
        TTNbt.store(tag, "values", Codec.STRING.listOf(), List.of("aer", "ignis"));
        assertEquals(14, TTNbt.read(tag, "count", Codec.INT, registries).orElseThrow());
        assertEquals(List.of("aer", "ignis"), TTNbt.read(tag, "values", Codec.STRING.listOf()).orElseThrow());
    }

    @Test
    void absentAndInvalidFieldsReturnEmptyAndFailedWritesKeepExistingData() {
        CompoundTag tag = new CompoundTag();
        assertTrue(TTNbt.read(tag, "missing", Codec.INT).isEmpty());
        tag.putString("wrong", "not an integer");
        assertTrue(TTNbt.read(tag, "wrong", Codec.INT).isEmpty());
        tag.putInt("existing", 42);
        Codec<Integer> refused = Codec.INT.flatXmap(DataResult::success, value -> DataResult.error(() -> "write rejected"));
        TTNbt.store(tag, "existing", refused, 1);
        assertEquals(42, tag.getInt("existing"));
    }

    @Test
    void nullableStoresPreserveAbsentAndExistingFields() {
        CompoundTag tag = new CompoundTag();
        var registries = mock(HolderLookup.Provider.class);
        TTNbt.storeNullable(tag, "absent", Codec.STRING, registries, null);
        assertFalse(tag.contains("absent"));
        tag.putString("existing", "keep");
        TTNbt.storeNullable(tag, "existing", Codec.STRING, null);
        assertEquals("keep", tag.getString("existing"));
        TTNbt.storeNullable(tag, "existing", Codec.STRING, registries, "new");
        assertEquals("new", tag.getString("existing"));
    }
}
