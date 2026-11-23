package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.InfusionManager")
public enum InfusionManager implements ProcessingManager<InfusionRecipe> {
    INSTANCE;

    @Override
    public RecipeType<InfusionRecipe> getRecipeType() {
        return TTRecipeTypes.INFUSION.get();
    }

    @ZenCodeType.Method
    public void addRecipe(String name, IItemStack output, IIngredient catalyst, IIngredient[] components,
            AspectAmount[] aspects, int instability, @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = definition(catalyst, components, aspects, instability, research);
        json.add("result", Recipes.stack(output));
        Recipes.add(this, name, json);
    }

    @ZenCodeType.Method
    public void addCatalystPatch(String name, MapData patch, IIngredient catalyst, IIngredient[] components,
            AspectAmount[] aspects, int instability, @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = definition(catalyst, components, aspects, instability, research);
        json.add("catalyst_patch", JsonParser.parseString(patch.accept(DataToJsonStringVisitor.INSTANCE)));
        Recipes.add(this, name, json);
    }

    private static JsonObject definition(IIngredient catalyst, IIngredient[] components, AspectAmount[] aspects,
            int instability, ResearchRequirement research) {
        var json = Recipes.recipe(InfusionRecipe.SERIALIZER, research);
        json.add("catalyst", Recipes.ingredient(catalyst));
        json.add("components", Recipes.ingredients(components));
        json.add("aspects", AspectAmount.definitions(aspects));
        json.addProperty("instability", instability);
        return json;
    }
}
