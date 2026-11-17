package com.leclowndu93150.thaumaturge.itemdata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/** Loot copy selection: omitted include means all; an empty include means none. */
public record ItemDataSelection(Optional<Set<ResourceLocation>> include, Set<ResourceLocation> exclude) {
    public static final ItemDataSelection ALL = new ItemDataSelection(Optional.empty(), Set.of());

    public ItemDataSelection {
        include = include.map(Set::copyOf);
        exclude = Set.copyOf(exclude);
    }

    public boolean includes(ResourceLocation id) {
        return include.map(ids -> ids.contains(id)).orElse(true) && !exclude.contains(id);
    }

    public void serialize(JsonObject output) {
        output.addProperty("source", "block_entity");
        include.ifPresent(ids -> output.add("include", array(ids)));
        if (!exclude.isEmpty()) output.add("exclude", array(exclude));
    }

    public static ItemDataSelection deserialize(JsonObject input) {
        String source = GsonHelper.getAsString(input, "source", "block_entity");
        if (!source.equals("block_entity")) throw new JsonSyntaxException("Unknown item data source " + source);
        return new ItemDataSelection(input.has("include") ? Optional.of(ids(input, "include")) : Optional.empty(),
                input.has("exclude") ? ids(input, "exclude") : Set.of());
    }

    private static Set<ResourceLocation> ids(JsonObject input, String field) {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (var element : GsonHelper.getAsJsonArray(input, field)) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new JsonSyntaxException("Invalid item field ID in " + field);
            }
            ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
            if (id == null) throw new JsonSyntaxException("Invalid item field ID " + element);
            ids.add(id);
        }
        return ids;
    }

    private static JsonArray array(Set<ResourceLocation> ids) {
        JsonArray output = new JsonArray();
        ids.stream().sorted().forEach(id -> output.add(id.toString()));
        return output;
    }
}
