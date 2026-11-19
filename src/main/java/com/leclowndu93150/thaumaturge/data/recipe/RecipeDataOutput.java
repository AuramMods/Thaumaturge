package com.leclowndu93150.thaumaturge.data.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

public final class RecipeDataOutput {
    private RecipeDataOutput() {}

    @SuppressWarnings("unchecked") // Each custom recipe supplies its own serializer and field codec.
    public static void saveCustom(
            Consumer<FinishedRecipe> output, ResourceLocation id, Recipe<?> recipe, @Nullable Unlock advancement) {
        if (!(recipe.getSerializer() instanceof SimpleRecipeSerializer<?> serializer)) {
            throw new IllegalArgumentException("Recipe " + id + " has no custom field codec");
        }
        var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryContext.resolve(null));
        var codec = (MapCodec<Recipe<?>>) serializer.codec();
        JsonObject fields = codec.codec().encodeStart(ops, recipe).getOrThrow(false, error -> {
            throw new JsonParseException("Could not write recipe " + id + ": " + error);
        }).getAsJsonObject();
        save(output, id, serializer, fields, advancement);
    }

    public static void save(
            Consumer<FinishedRecipe> output, ResourceLocation id, RecipeSerializer<?> serializer,
            JsonObject fields, @Nullable Unlock advancement) {
        output.accept(new Finished(id, serializer, fields, advancement));
    }

    public static Consumer<FinishedRecipe> withCondition(Consumer<FinishedRecipe> output, ICondition condition) {
        return recipe -> {
            var conditional = ConditionalRecipe.builder().addCondition(condition).addRecipe(recipe);
            if (recipe.getAdvancementId() != null) {
                conditional.generateAdvancement(recipe.getAdvancementId());
            }
            conditional.build(output, recipe.getId());
        };
    }

    public record Unlock(ResourceLocation id, Advancement.Builder builder) {}

    private record Finished(
            ResourceLocation id, RecipeSerializer<?> serializer, JsonObject fields, @Nullable Unlock advancement)
            implements FinishedRecipe {
        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return serializer;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            fields.entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
        }

        @Override
        public @Nullable JsonObject serializeAdvancement() {
            return advancement == null ? null : advancement.builder().serializeToJson();
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId() {
            return advancement == null ? null : advancement.id();
        }
    }
}
