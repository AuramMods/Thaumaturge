package com.leclowndu93150.thaumaturge.datamap;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.Test;

public class DataMapFileRuntimeTest {
    private static final ResourceKey<Registry<String>> ROOT = ResourceKey.createRegistryKey(new ResourceLocation("port_map_file", "values"));
    private static final ResourceKey<String> A = ResourceKey.create(ROOT, new ResourceLocation("port_map_file", "a"));
    private static final ResourceKey<String> B = ResourceKey.create(ROOT, new ResourceLocation("port_map_file", "b"));
    private static final TagKey<String> TAG = TagKey.create(ROOT, new ResourceLocation("port_map_file", "tag"));
    private static final DataMapType<String, Integer> SIMPLE = DataMapType.builder(new ResourceLocation("port_map_file", "simple"), ROOT, Codec.INT).build();
    private static final DataMapType<String, List<Integer>> MERGING = DataMapType.builder(new ResourceLocation("port_map_file", "merging"), ROOT, Codec.INT.listOf())
            .merger((registry, oldSource, oldValue, newSource, newValue) -> java.util.stream.Stream.concat(oldValue.stream(), newValue.stream()).toList()).build();
    private Registry<String> registry() {
        var registry = new MappedRegistry<String>(ROOT, Lifecycle.stable());
        registry.register(A, "a", Lifecycle.stable()); registry.register(B, "b", Lifecycle.stable()); return registry.freeze();
    }
    private <R, T> DataMapFile<R, T> read(DataMapType<R, T> type, String text) {
        return DataMapFile.read(type, JsonOps.INSTANCE, JsonParser.parseString(text), ICondition.IContext.EMPTY);
    }
    private <T> Map<ResourceKey<String>, T> build(Registry<String> registry, DataMapType<String, T> type, List<DataMapFile<String, T>> layers) {
        var errors = new ArrayList<String>(); var result = DataMapAssembler.build(registry, type, layers, errors::add); assertTrue(errors.isEmpty(), errors.toString()); return result;
    }

    @Test
    void defaultConflictsReplaceValuesAndWholeFileReplacementDropsLowerLayerEntries() {
        var registry = registry();
        var low = read(SIMPLE, "{\"values\":{\"port_map_file:a\":1,\"port_map_file:b\":2}}");
        var high = read(SIMPLE, "{\"values\":{\"port_map_file:a\":3}}");
        assertEquals(Map.of(A, 3, B, 2), build(registry, SIMPLE, List.of(low, high)));
        assertEquals(Map.of(A, 4), build(registry, SIMPLE, List.of(low, read(SIMPLE, "{\"replace\":true,\"values\":{\"port_map_file:a\":4}}"))));
    }

    @Test
    void tagsAreResolvedAtAssemblyAndEntryReplacementBypassesTheMerger() {
        var registry = registry();
        var tag = read(MERGING, "{\"values\":{\"#port_map_file:tag\":[1]}}");
        var direct = read(MERGING, "{\"values\":{\"port_map_file:a\":[2]}}");
        assertTrue(build(registry, MERGING, List.of(tag)).isEmpty());
        registry.bindTags(Map.of(TAG, List.of(registry.getHolderOrThrow(A), registry.getHolderOrThrow(B))));
        assertEquals(Map.of(A, List.of(1, 2), B, List.of(1)), build(registry, MERGING, List.of(tag, direct)));
        var replace = read(MERGING, "{\"values\":{\"port_map_file:a\":{\"value\":[3],\"replace\":true}}}");
        assertEquals(Map.of(A, List.of(3), B, List.of(1)), build(registry, MERGING, List.of(tag, direct, replace)));
        registry.bindTags(Map.of(TAG, List.of(registry.getHolderOrThrow(B))));
        assertEquals(Map.of(B, List.of(1)), build(registry, MERGING, List.of(tag)));
    }

    @Test
    void additionsRunBeforeRemovalsAndAbsentRemovalTargetsAreOptional() {
        var registry = registry(); registry.bindTags(Map.of(TAG, List.of(registry.getHolderOrThrow(A))));
        var layer = read(SIMPLE, "{\"values\":{\"port_map_file:a\":1,\"port_map_file:b\":2},\"remove\":[\"#port_map_file:tag\",\"port_map_file:missing\"]}");
        assertEquals(Map.of(B, 2), build(registry, SIMPLE, List.of(layer)));
    }

    @Test
    void sourceAndNativeConditionsFilterBeforeDecodingButValidateTheEntireConditionList() {
        var layer = read(SIMPLE, """
                {"values":{
                  "port_map_file:a":{"neoforge:conditions":[{"type":"neoforge:false"}],"neoforge:value":"invalid"},
                  "port_map_file:b":{"forge:conditions":[{"type":"forge:and","values":[{"type":"neoforge:true"},{"type":"forge:not","value":{"type":"neoforge:false"}}]}],"forge:value":9}}}
                """);
        assertEquals(Map.of(B, 9), build(registry(), SIMPLE, List.of(layer)));
        assertThrows(JsonParseException.class, () -> read(SIMPLE, "{\"values\":{\"port_map_file:a\":{\"neoforge:conditions\":[{\"type\":\"neoforge:false\"},{\"type\":\"missing:condition\"}],\"neoforge:value\":1}}}"));
        var item = read(SIMPLE, "{\"values\":{\"port_map_file:a\":{\"neoforge:conditions\":[{\"type\":\"neoforge:item_exists\",\"item\":\"minecraft:paper\"}],\"neoforge:value\":7}}}");
        assertEquals(Map.of(A, 7), build(registry(), SIMPLE, List.of(item)));
    }

    @Test
    void invalidPresentFieldsAndMixedAdvancedRemovalFormatsFailTheWholeLayer() {
        for (String text : List.of("{}", "{\"values\":false}", "{\"replace\":\"bad\",\"values\":{}}", "{\"values\":{\"bad key\":1}}", "{\"values\":{\"port_map_file:a\":\"bad\"}}", "{\"values\":{},\"remove\":{}}", "{\"values\":{},\"remove\":[{\"key\":\"port_map_file:a\"}]}")) {
            assertThrows(JsonParseException.class, () -> read(SIMPLE, text), text);
        }
        assertThrows(JsonParseException.class, () -> read(InfernalBonus.DATA_MAP, "{\"values\":{},\"remove\":[\"minecraft:paper\",{\"key\":\"minecraft:diamond\"}]}"));
    }

    @Test
    void actualInfernalBonusSupportsAllSourceTargetedRemovalForms() {
        String low = "{\"values\":{\"minecraft:iron_ingot\":[{\"items\":\"minecraft:paper\"},{\"items\":\"minecraft:diamond\"}]}}";
        var original = read(InfernalBonus.DATA_MAP, low);
        for (String removal : List.of("{\"minecraft:iron_ingot\":\"minecraft:paper\"}", "[{\"key\":\"minecraft:iron_ingot\",\"remover\":\"minecraft:paper\"}]")) {
            var high = read(InfernalBonus.DATA_MAP, "{\"values\":{},\"remove\":" + removal + "}");
            var errors = new ArrayList<String>(); var values = DataMapAssembler.build(BuiltInRegistries.ITEM, InfernalBonus.DATA_MAP, List.of(original, high), errors::add);
            assertTrue(errors.isEmpty()); var remaining = values.get(Items.IRON_INGOT.builtInRegistryHolder().key());
            assertEquals(1, remaining.size()); assertTrue(remaining.get(0).items().contains(Items.DIAMOND.builtInRegistryHolder()));
        }
        for (String removal : List.of("[\"minecraft:iron_ingot\"]", "[{\"key\":\"minecraft:iron_ingot\"}]")) {
            assertTrue(DataMapAssembler.build(BuiltInRegistries.ITEM, InfernalBonus.DATA_MAP,
                    List.of(original, read(InfernalBonus.DATA_MAP, "{\"values\":{},\"remove\":" + removal + "}")), fail -> fail(fail)).isEmpty());
        }
    }

    @Test
    void unknownRequiredObjectsProduceAnExplicitDiagnosticAndDoNotAliasDefaultValues() {
        var errors = new ArrayList<String>(); var registry = registry();
        var result = DataMapAssembler.build(registry, SIMPLE, List.of(read(SIMPLE, "{\"values\":{\"port_map_file:missing\":1,\"port_map_file:a\":2}}")), errors::add);
        assertEquals(Map.of(A, 2), result); assertEquals(1, errors.size()); assertTrue(errors.get(0).contains("port_map_file:missing"));
    }
}
