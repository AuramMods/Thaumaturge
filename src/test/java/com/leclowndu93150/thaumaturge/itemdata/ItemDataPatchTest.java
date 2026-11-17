package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ItemDataPatchTest {
    private final ItemDataRegistry registry = new ItemDataRegistry("thaumaturge");
    private final ItemDataKey<Integer> charge = registry.<Integer>registerComponentType("charge", b -> b.persistent(Codec.intRange(0, 100))).get();
    private final ItemDataKey<String> label = registry.<String>registerComponentType("label", b -> b.persistent(Codec.STRING)).get();
    private final ItemDataKey<List<Integer>> list = registry.<List<Integer>>registerComponentType("list", b -> b.persistent(Codec.INT.listOf())).get();
    private final Codec<ItemDataPatch> codec = ItemDataPatch.codec(registry::get);

    @Test
    void heterogeneousValuesAndRemovalKeepBaselineJsonNames() {
        ItemDataPatch patch = ItemDataPatch.builder().set(charge, 70).set(list, List.of(2, 4)).remove(label).build();
        var encoded = codec.encodeStart(JsonOps.INSTANCE, patch).getOrThrow(false, message -> fail(message));
        assertEquals(JsonParser.parseString("{\"thaumaturge:charge\":70,\"thaumaturge:list\":[2,4],\"!thaumaturge:label\":{}}"), encoded);
        assertEquals(patch, codec.parse(JsonOps.INSTANCE, encoded).getOrThrow(false, message -> fail(message)));
        var nbt = codec.encodeStart(NbtOps.INSTANCE, patch).getOrThrow(false, message -> fail(message));
        assertEquals(patch, codec.parse(NbtOps.INSTANCE, nbt).getOrThrow(false, message -> fail(message)));
    }

    @Test
    void builderSnapshotsEntriesAndLastChangeWins() {
        var builder = ItemDataPatch.builder().set(charge, 1);
        ItemDataPatch first = builder.build();
        builder.set(charge, 2).remove(label);
        assertNotEquals(first, builder.build());
        assertEquals(1, first.entrySet().size());
        assertEquals(1, first.entrySet().iterator().next().getValue().orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> first.entrySet().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.entrySet().iterator().next().setValue(java.util.Optional.empty()));
        assertTrue(ItemDataPatch.builder().build().isEmpty());
        assertSame(ItemDataPatch.EMPTY, codec.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).result().orElseThrow());
    }

    @Test
    void malformedUnknownDuplicateAndInvalidFieldsFail() {
        for (String json : List.of("[]", "{\"bad id\":2}", "{\"addon:unknown\":2}",
                "{\"thaumaturge:charge\":101}", "{\"thaumaturge:charge\":\"x\"}",
                "{\"!thaumaturge:label\":true}", "{\"!thaumaturge:label\":{\"x\":1}}",
                "{\"thaumaturge:charge\":1,\"!thaumaturge:charge\":{}}")) {
            assertTrue(codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).error().isPresent(), json);
        }
        assertTrue(codec.encodeStart(JsonOps.INSTANCE, ItemDataPatch.builder().set(charge, -1).build()).error().isPresent());
        assertThrows(NullPointerException.class, () -> ItemDataPatch.builder().set(label, null));
    }

    @Test
    void predicatesKeepExpectedValuesAndRejectRemovalPatches() {
        var predicateCodec = ItemDataPredicate.codec(registry::get);
        ItemDataPredicate predicate = ItemDataPredicate.builder().expect(charge, 4).expect(label, "vis").build();
        var encoded = predicateCodec.encodeStart(JsonOps.INSTANCE, predicate).result().orElseThrow();
        assertEquals(predicate, predicateCodec.parse(JsonOps.INSTANCE, encoded).result().orElseThrow());
        assertTrue(predicateCodec.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"!thaumaturge:charge\":{}}")).error().isPresent());
        assertThrows(IllegalArgumentException.class, () -> new ItemDataPredicate(ItemDataPatch.builder().remove(charge).build()));
        assertTrue(ItemDataPredicate.builder().build().isEmpty());
    }

    @Test
    void registryAwareWireCodecCarriesTypedValuesAndRemovals() {
        var wire = ByteBufCodecs.fromCodecWithRegistries(codec);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), mock(RegistryAccess.class));
        try {
            ItemDataPatch patch = ItemDataPatch.builder().set(charge, 100).remove(label).build();
            wire.encode(buffer, patch);
            assertEquals(patch, wire.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally { buffer.release(); }
    }

    @Test
    void storedEnchantmentRequirementDecodesFromBaselineResearchJson() throws Exception {
        var path = java.nio.file.Path.of(System.getProperty("thaumaturge.testResourceRoot"),
                "data/thaumaturge/thaumaturge/research_entry/focus_break.json");
        var research = JsonParser.parseString(java.nio.file.Files.readString(path)).getAsJsonObject();
        var components = research.getAsJsonArray("stages").get(0).getAsJsonObject()
                .getAsJsonArray("obtain").get(0).getAsJsonObject().getAsJsonObject("components");
        ItemDataPatch patch = ItemDataPatch.CODEC.parse(JsonOps.INSTANCE, components).getOrThrow(false, message -> fail(message));
        assertEquals(1, patch.entrySet().size());
        var entry = patch.entrySet().iterator().next();
        assertSame(VanillaItemData.STORED_ENCHANTMENTS, entry.getKey());
        assertEquals(new EnchantmentData(Map.of(ResourceLocation.parse("minecraft:fortune"), 1), true), entry.getValue().orElseThrow());
    }
}
