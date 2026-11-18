package com.leclowndu93150.thaumaturge.datamap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

/** Decoded pack layer; selectors remain unresolved until native tags have been rebound. */
public record DataMapFile<R, T>(boolean replace, List<Addition<R, T>> values, List<Removal<R, T>> removals) {
    public DataMapFile { values = List.copyOf(values); removals = List.copyOf(removals); }
    public record Entry<T>(T value, boolean replace) {}
    public record Addition<R, T>(Either<TagKey<R>, ResourceKey<R>> key, Entry<T> entry) {}
    public record Removal<R, T>(Either<TagKey<R>, ResourceKey<R>> key, Optional<DataMapValueRemover<R, T>> remover) {}

    private static <T> Codec<Entry<T>> entryCodec(Codec<T> value) {
        Codec<Entry<T>> wrapped = RecordCodecBuilder.create(i -> i.group(
                value.fieldOf("value").forGetter(Entry::value),
                Codecs.optionalFieldOf(Codec.BOOL, "replace", false).forGetter(Entry::replace)).apply(i, Entry::new));
        return Codec.either(wrapped, value).xmap(e -> e.map(v -> v, v -> new Entry<>(v, false)),
                entry -> entry.replace() ? Either.left(entry) : Either.right(entry.value()));
    }

    public static <R, T> DataMapFile<R, T> read(DataMapType<R, T> type, DynamicOps<JsonElement> ops,
            JsonElement input, ICondition.IContext context) {
        if (ops.compressMaps()) throw new JsonParseException("Data maps do not support compressed maps");
        var conditional = conditionalValue(input, context);
        if (conditional.isEmpty()) return new DataMapFile<>(false, List.of(), List.of());
        JsonObject object = GsonHelper.convertToJsonObject(conditional.get(), "data map");
        boolean replace = object.has("replace") && read(Codec.BOOL, ops, object.get("replace"));
        JsonObject values = GsonHelper.getAsJsonObject(object, "values");
        var codec = entryCodec(type.codec());
        List<Addition<R, T>> additions = new ArrayList<>();
        for (var value : values.entrySet()) {
            var key = selector(type, value.getKey());
            conditionalValue(value.getValue(), context).ifPresent(data -> additions.add(new Addition<>(key, read(codec, ops, data))));
        }
        List<Removal<R, T>> removals = new ArrayList<>();
        if (object.has("remove")) {
            JsonElement remove = object.get("remove");
            if (remove.isJsonObject() && type.remover() != null) {
                remove.getAsJsonObject().entrySet().forEach(e -> removals.add(new Removal<>(selector(type, e.getKey()),
                        Optional.of(read(type.remover(), ops, e.getValue())))));
            } else {
                var list = GsonHelper.convertToJsonArray(remove, "remove");
                boolean records = type.remover() != null && !list.isEmpty() && list.get(0).isJsonObject();
                for (JsonElement element : list) {
                    if (records) {
                        JsonObject record = GsonHelper.convertToJsonObject(element, "removal");
                        removals.add(new Removal<>(selector(type, read(Codec.STRING, ops, record.get("key"))),
                                record.has("remover") ? Optional.of(read(type.remover(), ops, record.get("remover"))) : Optional.empty()));
                    } else removals.add(new Removal<>(selector(type, read(Codec.STRING, ops, element)), Optional.empty()));
                }
            }
        }
        return new DataMapFile<>(replace, additions, removals);
    }

    public static <R, T> Either<TagKey<R>, ResourceKey<R>> selector(DataMapType<R, T> type, String text) {
        boolean tag = text.startsWith("#");
        ResourceLocation id = ResourceLocation.tryParse(tag ? text.substring(1) : text);
        if (id == null) throw new JsonParseException("Invalid data map selector " + text);
        return tag ? Either.left(TagKey.create(type.registryKey(), id)) : Either.right(ResourceKey.create(type.registryKey(), id));
    }

    private static <T> T read(Codec<T> codec, DynamicOps<JsonElement> ops, JsonElement input) {
        if (input == null) throw new JsonParseException("Missing required data map value");
        return codec.parse(ops, input).getOrThrow(false, message -> { throw new JsonParseException(message); });
    }

    private static Optional<JsonElement> conditionalValue(JsonElement value, ICondition.IContext context) {
        if (!value.isJsonObject()) return Optional.of(value);
        JsonObject object = value.getAsJsonObject();
        String key = object.has("neoforge:conditions") ? "neoforge:conditions" : "forge:conditions";
        if (!object.has(key)) return Optional.of(value);
        if (object.has("neoforge:conditions") && object.has("forge:conditions")) throw new JsonParseException("Ambiguous data map conditions");
        var conditions = new ArrayList<ICondition>();
        for (JsonElement condition : GsonHelper.getAsJsonArray(object, key)) {
            conditions.add(CraftingHelper.getCondition(nativeCondition(GsonHelper.convertToJsonObject(condition, "condition"))));
        }
        if (!conditions.stream().allMatch(condition -> condition.test(context))) return Optional.empty();
        String carrier = key.equals("neoforge:conditions") ? "neoforge:value" : "forge:value";
        if (object.has(carrier)) return Optional.of(object.get(carrier));
        var filtered = object.deepCopy(); filtered.remove(key); return Optional.of(filtered);
    }

    /** Pinned source built-ins have identical native serializers, including nested condition fields. */
    private static JsonObject nativeCondition(JsonObject condition) {
        JsonObject result = condition.deepCopy();
        String type = GsonHelper.getAsString(result, "type");
        switch (type) {
            case "neoforge:and", "neoforge:or", "forge:and", "forge:or" -> {
                var children = GsonHelper.getAsJsonArray(result, "values");
                for (int i = 0; i < children.size(); i++) children.set(i, nativeCondition(GsonHelper.convertToJsonObject(children.get(i), "condition")));
            }
            case "neoforge:not", "forge:not" -> result.add("value", nativeCondition(GsonHelper.getAsJsonObject(result, "value")));
        }
        switch (type) {
            case "neoforge:and", "neoforge:or", "neoforge:not", "neoforge:true", "neoforge:false",
                    "neoforge:item_exists", "neoforge:tag_empty", "neoforge:mod_loaded" -> result.addProperty("type", "forge:" + type.substring("neoforge:".length()));
        }
        return result;
    }
}
