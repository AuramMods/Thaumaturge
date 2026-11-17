package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ItemDataSelectionTest {
    private final ResourceLocation contents = ResourceLocation.parse("thaumaturge:essentia_contents");
    private final ResourceLocation label = ResourceLocation.parse("thaumaturge:aspect_filter");

    @Test
    void absentAndEmptyIncludeHaveDifferentMeanings() {
        ItemDataSelection all = read("{\"source\":\"block_entity\"}");
        assertTrue(all.includes(contents));
        assertTrue(all.includes(label));
        ItemDataSelection none = read("{\"source\":\"block_entity\",\"include\":[]}");
        assertFalse(none.includes(contents));
        assertFalse(none.includes(label));
    }

    @Test
    void includeAndExcludeRoundTripAsStableFieldIds() {
        ItemDataSelection selection = new ItemDataSelection(Optional.of(Set.of(contents, label)), Set.of(label));
        assertTrue(selection.includes(contents));
        assertFalse(selection.includes(label));
        JsonObject encoded = new JsonObject();
        selection.serialize(encoded);
        assertEquals(selection, ItemDataSelection.deserialize(encoded));
        assertEquals("block_entity", encoded.get("source").getAsString());
        assertEquals(2, encoded.getAsJsonArray("include").size());
        assertEquals("thaumaturge:aspect_filter", encoded.getAsJsonArray("exclude").get(0).getAsString());
    }

    @Test
    void selectionSnapshotsInputsAndAllowsFutureNamespacedFields() {
        Set<ResourceLocation> included = new LinkedHashSet<>(Set.of(contents));
        ItemDataSelection selection = new ItemDataSelection(Optional.of(included), Set.of());
        included.clear();
        assertTrue(selection.includes(contents));
        assertThrows(UnsupportedOperationException.class, () -> selection.include().orElseThrow().clear());
        assertTrue(read("{\"include\":[\"addon:future\"]}").includes(ResourceLocation.parse("addon:future")));
    }

    @Test
    void malformedIdsListsAndSourcesReportJsonErrors() {
        for (String json : new String[]{"{\"source\":\"this\"}", "{\"include\":true}",
                "{\"include\":[1]}", "{\"include\":[\"bad id\"]}", "{\"exclude\":{}}"}) {
            assertThrows(JsonSyntaxException.class, () -> read(json), json);
        }
    }

    private ItemDataSelection read(String json) {
        return ItemDataSelection.deserialize(JsonParser.parseString(json).getAsJsonObject());
    }
}
