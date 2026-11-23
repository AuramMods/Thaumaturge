package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapelessCraftingRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import java.util.Map;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.ArcaneWorkbenchManager")
public enum ArcaneWorkbenchManager implements ProcessingManager<ArcaneCraftingRecipe> {
    INSTANCE;

    @Override
    public RecipeType<ArcaneCraftingRecipe> getRecipeType() {
        return TTRecipeTypes.ARCANE.get();
    }

    @ZenCodeType.Method
    public void addShaped(String name, IItemStack output, int vis, AspectAmount[] crystals,
            String[] pattern, Map<String, IIngredient> key,
            @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = Recipes.recipe(ArcaneShapedCraftingRecipe.SERIALIZER, research);
        addCostsAndResult(json, output, vis, crystals);
        var rows = new JsonArray();
        for (String row : pattern) rows.add(row);
        json.add("pattern", rows);
        var symbols = new JsonObject();
        key.forEach((symbol, ingredient) -> symbols.add(symbol, Recipes.ingredient(ingredient)));
        json.add("key", symbols);
        Recipes.add(this, name, json);
    }

    @ZenCodeType.Method
    public void addShapeless(String name, IItemStack output, int vis, AspectAmount[] crystals,
            IIngredient[] ingredients, @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = Recipes.recipe(ArcaneShapelessCraftingRecipe.SERIALIZER, research);
        addCostsAndResult(json, output, vis, crystals);
        json.add("ingredients", Recipes.ingredients(ingredients));
        Recipes.add(this, name, json);
    }

    private static void addCostsAndResult(JsonObject json, IItemStack output, int vis, AspectAmount[] crystals) {
        json.addProperty("vis", vis);
        json.add("crystals", AspectAmount.definitions(crystals));
        json.add("result", Recipes.stack(output));
    }
}
