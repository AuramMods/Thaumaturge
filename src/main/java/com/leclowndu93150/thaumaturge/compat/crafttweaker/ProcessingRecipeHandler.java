package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.type.WrappingIIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.util.StringUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionEnchantmentRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapelessCraftingRecipe;
import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;
import com.leclowndu93150.thaumaturge.serialization.IngredientCodecs;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ArcaneShapedCraftingRecipe.class)
@IRecipeHandler.For(ArcaneShapelessCraftingRecipe.class)
@IRecipeHandler.For(CrucibleRecipe.class)
@IRecipeHandler.For(InfusionRecipe.class)
@IRecipeHandler.For(InfusionEnchantmentRecipe.class)
@IRecipeHandler.For(InfusionRunicAugmentRecipe.class)
@IRecipeHandler.For(DustTriggerSimpleRecipe.class)
@IRecipeHandler.For(DustTriggerTagRecipe.class)
@IRecipeHandler.For(DustTriggerMultiblockRecipe.class)
public final class ProcessingRecipeHandler implements IRecipeHandler<Recipe<?>> {
    @Override
    public String dumpToCommandString(IRecipeManager<? super Recipe<?>> manager, Recipe<?> recipe) {
        return manager.getCommandString() + ".addJson(" + StringUtil.quoteAndEscape(recipe.getId())
                + ", " + StringUtil.quoteAndEscape(Recipes.definition(recipe).toString()) + ");";
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(
            IRecipeManager<? super Recipe<?>> manager, Recipe<?> first, U second) {
        return first.getClass() == second.getClass() && matchingDefinition(first).equals(matchingDefinition(second));
    }

    private static JsonObject matchingDefinition(Recipe<?> recipe) {
        JsonObject json = Recipes.definition(recipe);
        for (String field : List.of("group", "result", "catalyst_patch", "instability", "vis")) json.remove(field);
        return json;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super Recipe<?>> manager, Recipe<?> recipe) {
        JsonObject json = Recipes.definition(recipe);
        var builder = IDecomposedRecipe.builder().with(RecipeComponents.DEFINITION, json.toString());
        List<IIngredient> ingredients = inputs(json).stream().map(InputSlot::value)
                .map(ProcessingRecipeHandler::ingredient).toList();
        if (!ingredients.isEmpty()) builder.with(BuiltinRecipeComponents.Input.INGREDIENTS, ingredients);
        if (json.has("result")) {
            builder.with(BuiltinRecipeComponents.Output.ITEMS,
                    IItemStack.of(Recipes.decode(ItemStackCodecs.CODEC, json.get("result"))));
        }
        return Optional.of(builder.build());
    }

    @Override
    public Optional<Recipe<?>> recompose(IRecipeManager<? super Recipe<?>> manager, ResourceLocation id,
            IDecomposedRecipe recipe) {
        JsonObject definition = JsonParser.parseString(recipe.getOrThrowSingle(RecipeComponents.DEFINITION)).getAsJsonObject();
        List<InputSlot> slots = inputs(definition);
        if (!slots.isEmpty()) {
            List<IIngredient> replacements = recipe.getOrThrow(BuiltinRecipeComponents.Input.INGREDIENTS);
            if (slots.size() != replacements.size()) throw new IllegalArgumentException("Input count changed for " + id);
            for (int index = 0; index < slots.size(); index++) {
                slots.get(index).replace().accept(Recipes.ingredient(replacements.get(index)));
            }
        }
        if (definition.has("result")) {
            definition.add("result", Recipes.stack(recipe.getOrThrowSingle(BuiltinRecipeComponents.Output.ITEMS)));
        }
        Recipe<?> rebuilt = Recipes.decode(id, definition);
        if (rebuilt.getType() != manager.getRecipeType()) {
            throw new IllegalArgumentException("Recomposed recipe " + id + " has the wrong type");
        }
        return Optional.of(rebuilt);
    }

    private static IIngredient ingredient(JsonElement value) {
        var nativeIngredient = Recipes.decode(IngredientCodecs.CODEC, value);
        IIngredient converted = IIngredient.fromIngredient(nativeIngredient);
        // CraftTweaker converts some Forge ingredients to a list of example stacks.
        return value.equals(Recipes.ingredient(converted)) ? converted
                : new WrappingIIngredient(nativeIngredient, value.toString());
    }

    private record InputSlot(JsonElement value, Consumer<JsonElement> replace) {}

    static boolean replaceInput(JsonObject definition, JsonElement from, JsonElement to) {
        List<InputSlot> found = inputs(definition).stream().filter(slot -> slot.value().equals(from)).toList();
        found.forEach(slot -> slot.replace().accept(to));
        return !found.isEmpty();
    }

    private static List<InputSlot> inputs(JsonObject json) {
        List<InputSlot> slots = new ArrayList<>();
        for (String field : List.of("catalyst", "display_catalyst", "per_level")) {
            if (json.has(field)) slots.add(new InputSlot(json.get(field), value -> json.add(field, value)));
        }
        if (json.has("key")) {
            JsonObject key = json.getAsJsonObject("key");
            key.keySet().stream().sorted().forEach(symbol ->
                    slots.add(new InputSlot(key.get(symbol), value -> key.add(symbol, value))));
        }
        for (String field : List.of("ingredients", "components")) {
            if (!json.has(field)) continue;
            var array = json.getAsJsonArray(field);
            for (int index = 0; index < array.size(); index++) {
                int slot = index;
                slots.add(new InputSlot(array.get(slot), value -> array.set(slot, value)));
            }
        }
        return slots;
    }
}
