package com.leclowndu93150.thaumaturge.serialization;

import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.recipe.ingredient.CodecIngredientSerializer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CompoundIngredient;

/** Item/tag and registered Forge custom ingredients with the 1.21 list/object entry points. */
public final class IngredientCodecs {
    private IngredientCodecs() {}

    private static final Codec<Ingredient> ENTRY = new Codec<>() {
        @Override public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            JsonElement json = ops.convertTo(JsonOps.INSTANCE, input);
            if (!json.isJsonObject()) return DataResult.error(() -> "Expected an ingredient object: " + json);
            try {
                var type = json.getAsJsonObject().get("type");
                if (type != null && type.isJsonPrimitive() && type.getAsJsonPrimitive().isString()) {
                    ResourceLocation id = ResourceLocation.tryParse(type.getAsString());
                    var owned = id == null ? null : CodecIngredientSerializer.registered(id);
                    if (owned != null) return owned.decode(ops, input).map(value -> Pair.of(value, input));
                }
                return DataResult.success(Pair.of(Ingredient.fromJson(json, false), input));
            } catch (RuntimeException failure) {
                return DataResult.error(() -> "Invalid ingredient: " + failure.getMessage());
            }
        }

        @Override public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
            return write(input, ops, prefix);
        }
    };

    private static final Codec<List<Ingredient>> ALTERNATIVES = Codecs.list(ENTRY, 0, Integer.MAX_VALUE);

    public static final Codec<Ingredient> CODEC = new Codec<>() {
        @Override public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            if (ops.convertTo(JsonOps.INSTANCE, input).isJsonArray()) {
                return ALTERNATIVES.decode(ops, input).map(pair -> pair.mapFirst(IngredientCodecs::combine));
            }
            return ENTRY.decode(ops, input);
        }

        @Override public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
            return write(input, ops, prefix);
        }
    };

    private static Ingredient combine(List<Ingredient> values) {
        if (values.isEmpty()) return Ingredient.EMPTY;
        if (values.size() == 1) return values.get(0);
        return CompoundIngredient.of(values.toArray(Ingredient[]::new));
    }

    private static <T> DataResult<T> write(Ingredient input, DynamicOps<T> ops, T prefix) {
        try {
            if (input.getSerializer() instanceof CodecIngredientSerializer<?> owned) {
                return owned.encodeTagged(input, ops, prefix);
            }
            if (input instanceof CompoundIngredient compound) {
                List<Ingredient> values = new ArrayList<>();
                for (Ingredient child : compound.getChildren()) flattenIngredients(child, values);
                return ALTERNATIVES.encode(values, ops, prefix);
            }
            JsonElement json = input.toJson();
            if (json.isJsonArray()) {
                List<Ingredient> values = new ArrayList<>();
                flattenIngredients(input, values);
                return ALTERNATIVES.encode(values, ops, prefix);
            }
            return Codec.PASSTHROUGH.encode(new Dynamic<>(JsonOps.INSTANCE, json), ops, prefix);
        } catch (RuntimeException failure) {
            return DataResult.error(() -> "Cannot encode ingredient: " + failure.getMessage());
        }
    }

    private static void flattenIngredients(Ingredient value, List<Ingredient> output) {
        if (value instanceof CompoundIngredient compound) {
            for (Ingredient child : compound.getChildren()) flattenIngredients(child, output);
        } else if (value.isVanilla() && value.toJson().isJsonArray()) {
            for (JsonElement child : value.toJson().getAsJsonArray()) output.add(Ingredient.fromJson(child, false));
        } else output.add(value);
    }
}
