package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

final class Recipes {
    private Recipes() {}

    static RegistryAccess registries() {
        // CraftTweaker binds the registry access of the resources currently being loaded.
        return CraftTweakerAPI.getAccessibleElementsProvider().server().registryAccess();
    }

    static <T extends Recipe<?>> void add(IRecipeManager<T> manager, String name, JsonObject definition) {
        ResourceLocation id = name.contains(":") ? new ResourceLocation(name)
                : new ResourceLocation(CraftTweakerConstants.MOD_ID, manager.fixRecipeName(name));
        write(manager, id, definition, false);
    }

    static <T extends Recipe<?>> void replace(IRecipeManager<T> manager, ResourceLocation id, JsonObject definition) {
        write(manager, id, definition, true);
    }

    private static <T extends Recipe<?>> void write(
            IRecipeManager<T> manager, ResourceLocation id, JsonObject definition, boolean replace) {
        Recipe<?> recipe = RegistryContext.withRegistries(registries(), () -> RecipeManager.fromJson(id, definition));
        if (recipe.getType() != manager.getRecipeType()) {
            throw new IllegalArgumentException("Recipe " + id + " has type " + recipe.getType()
                    + ", expected " + manager.getBracketResourceLocation());
        }
        var existing = CraftTweakerAPI.getAccessibleElementsProvider().accessibleRecipeManager()
                .crafttweaker$getByName().get(id);
        if (existing != null && existing.getType() != recipe.getType()) {
            throw new IllegalArgumentException("Recipe " + id + " already belongs to " + existing.getType());
        }
        if (replace) {
            if (existing == null) throw new IllegalArgumentException("Cannot replace missing recipe " + id);
            manager.removeByName(id.toString());
        }
        @SuppressWarnings("unchecked")
        T typed = (T) recipe;
        CraftTweakerAPI.apply(new ActionAddRecipe<>(manager, typed));
    }

    static JsonObject definition(Recipe<?> recipe) {
        var serializer = recipe.getSerializer();
        if (!(serializer instanceof SimpleRecipeSerializer<?> simple)) {
            throw new IllegalArgumentException("No codec for recipe " + recipe.getId());
        }
        @SuppressWarnings("unchecked")
        Codec<Recipe<?>> codec = (Codec<Recipe<?>>) (Codec<?>) simple.codec().codec();
        JsonObject json = encode(codec, recipe).getAsJsonObject();
        json.addProperty("type", BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer).toString());
        return json;
    }

    static <T> JsonElement encode(Codec<T> codec, T value) {
        RegistryAccess registries = registries();
        return RegistryContext.withRegistries(registries, () ->
                codec.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), value)
                        .getOrThrow(false, message -> { throw new IllegalArgumentException(message); }));
    }
}
