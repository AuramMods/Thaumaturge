package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import static com.leclowndu93150.thaumaturge.compat.dynamictrees.DynamicTreesJoCodeResourceTest.json;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

class DynamicTreesPlacementResourceTest {
    @Test
    void worldgenKeepsNativePlacementAndSelectionInsteadOfAddingPoissonSpawns() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        assertNull(loader.getResource("trees/thaumaturge/world_gen/default.json"));
        assertNull(loader.getResource("data/thaumaturge/neoforge/biome_modifier/dynamic_trees_worldgen.json"));
        assertNull(loader.getResource("data/thaumaturge/forge/biome_modifier/dynamic_trees_worldgen.json"));
        for (String tree : new String[] {"greatwood", "silverwood"}) {
            assertEquals(
                    "thaumaturge:compat_tree",
                    json("data/thaumaturge/worldgen/configured_feature/" + tree + "_tree.json")
                            .get("type")
                            .getAsString());
            assertEquals(
                    "minecraft:tree",
                    json("data/thaumaturge/worldgen/configured_feature/" + tree + "_tree_grown.json")
                            .get("type")
                            .getAsString());
        }
        assertRarity("greatwood_natural", 25);
        assertRarity("greatwood_natural_rare", 125);
        assertRarity("silverwood_natural", 60);
        JsonObject selection = json("data/thaumaturge/worldgen/configured_feature/magic_forest_trees.json");
        var choices = selection.getAsJsonObject("config").getAsJsonArray("features");
        assertEquals(
                1.0F / 18.0F, choices.get(0).getAsJsonObject().get("chance").getAsFloat());
        assertEquals(
                1.0F / 12.0F, choices.get(1).getAsJsonObject().get("chance").getAsFloat());
    }

    private static void assertRarity(String feature, int rarity) throws Exception {
        var placements = json("data/thaumaturge/worldgen/placed_feature/" + feature + ".json")
                .getAsJsonArray("placement");
        assertEquals(
                "minecraft:rarity_filter",
                placements.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals(rarity, placements.get(0).getAsJsonObject().get("chance").getAsInt());
    }
}
