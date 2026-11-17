package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class EnchantmentDataTest {
    private final ResourceLocation fortune = ResourceLocation.parse("minecraft:fortune");
    private final ResourceLocation silk = ResourceLocation.parse("minecraft:silk_touch");

    @Test
    void namedLevelsAndNativeStoredBookNbtRoundTrip() {
        EnchantmentData data = new EnchantmentData(Map.of(fortune, 3), false);
        var json = EnchantmentData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
        assertEquals(JsonParser.parseString("{\"levels\":{\"minecraft:fortune\":3},\"show_in_tooltip\":false}"), json);
        assertEquals(data, EnchantmentData.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow());
        ListTag nbt = data.toNbt();
        assertEquals("minecraft:fortune", nbt.getCompound(0).getString("id"));
        assertEquals(3, nbt.getCompound(0).getShort("lvl"));
        assertEquals(data, EnchantmentData.fromNbt(nbt, false));
    }

    @Test
    void researchRequiresAtLeastTheDeclaredLevelsAndAllowsOtherEnchantments() {
        EnchantmentData required = new EnchantmentData(Map.of(fortune, 1), true);
        assertTrue(new EnchantmentData(Map.of(fortune, 3, silk, 1), false).containsAtLeast(required));
        assertFalse(EnchantmentData.EMPTY.containsAtLeast(required));
        assertFalse(new EnchantmentData(Map.of(fortune, 1), true).containsAtLeast(new EnchantmentData(Map.of(fortune, 2), true)));
        assertTrue(required.containsAtLeast(EnchantmentData.EMPTY));
    }

    @Test
    void levelsAreIndependentOfMutableInputAndZeroIsCanonicalAbsence() {
        Map<ResourceLocation, Integer> input = new LinkedHashMap<>(Map.of(fortune, 2, silk, 0));
        EnchantmentData data = new EnchantmentData(input, true);
        input.put(fortune, 9);
        assertEquals(2, data.getLevel(fortune));
        assertEquals(0, data.getLevel(silk));
        assertEquals(1, data.keySet().size());
        assertThrows(UnsupportedOperationException.class, () -> data.levels().clear());
        assertEquals(data, EnchantmentData.fromNbt(data.toNbt(), true));
    }

    @Test
    void malformedLevelsAndNativeEntriesFailExplicitly() {
        assertThrows(IllegalArgumentException.class, () -> new EnchantmentData(Map.of(fortune, -1), true));
        assertThrows(IllegalArgumentException.class, () -> new EnchantmentData(Map.of(fortune, 256), true));
        assertTrue(EnchantmentData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"levels\":{\"minecraft:fortune\":256}}")).error().isPresent());
        ListTag invalid = new ListTag();
        invalid.add(new CompoundTag());
        assertThrows(IllegalArgumentException.class, () -> EnchantmentData.fromNbt(invalid, true));
        invalid.clear();
        invalid.add(StringTag.valueOf("wrong"));
        assertThrows(IllegalArgumentException.class, () -> EnchantmentData.fromNbt(invalid, true));
        CompoundTag item = new CompoundTag();
        item.put("StoredEnchantments", invalid);
        assertThrows(IllegalArgumentException.class, () -> EnchantmentData.readItemTag(item, "StoredEnchantments", 32));
    }

    @Test
    void optionalTooltipDefaultsDoNotHideMalformedJsonOrNativeNbt() {
        var json = JsonParser.parseString("{\"levels\":{},\"show_in_tooltip\":\"bad\"}");
        assertTrue(EnchantmentData.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent());
        CompoundTag nbt = new CompoundTag();
        nbt.put("levels", new CompoundTag());
        nbt.putString("show_in_tooltip", "bad");
        var invalid = EnchantmentData.CODEC.parse(NbtOps.INSTANCE, nbt);
        assertTrue(invalid.error().isPresent());
        assertTrue(invalid.result().isEmpty());
        nbt.remove("show_in_tooltip");
        assertTrue(EnchantmentData.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow().showInTooltip());
        nbt.putBoolean("show_in_tooltip", false);
        var hidden = EnchantmentData.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow();
        assertFalse(hidden.showInTooltip());
        assertEquals(nbt, EnchantmentData.CODEC.encodeStart(NbtOps.INSTANCE, hidden).result().orElseThrow());
        CompoundTag defaults = (CompoundTag) EnchantmentData.CODEC.encodeStart(NbtOps.INSTANCE, EnchantmentData.EMPTY)
                .result().orElseThrow();
        assertFalse(defaults.contains("show_in_tooltip"));
    }

    @Test
    void nativeTagUpdatesPreserveOtherFieldsAndTooltipFlags() {
        CompoundTag item = new CompoundTag();
        item.putString("addon:data", "keep");
        item.putInt("HideFlags", 2);
        new EnchantmentData(Map.of(fortune, 2), false).writeItemTag(item, "StoredEnchantments", 32);
        assertEquals(34, item.getInt("HideFlags"));
        assertEquals("keep", item.getString("addon:data"));
        assertEquals(new EnchantmentData(Map.of(fortune, 2), false), EnchantmentData.readItemTag(item, "StoredEnchantments", 32));
        EnchantmentData.EMPTY.writeItemTag(item, "StoredEnchantments", 32);
        assertFalse(item.contains("StoredEnchantments"));
        assertEquals(2, item.getInt("HideFlags"));
        assertEquals(EnchantmentData.EMPTY, EnchantmentData.readItemTag(item, "StoredEnchantments", 32));
        assertEquals(EnchantmentData.EMPTY, EnchantmentData.readItemTag(null, "StoredEnchantments", 32));
    }
}
