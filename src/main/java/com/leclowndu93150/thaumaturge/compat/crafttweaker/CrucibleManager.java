package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.serialization.IngredientCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.CrucibleManager")
public enum CrucibleManager implements IRecipeManager<CrucibleRecipe> {
    INSTANCE;

    @Override
    public RecipeType<CrucibleRecipe> getRecipeType() {
        return TTRecipeTypes.CRUCIBLE.get();
    }

    @ZenCodeType.Method
    public void addRecipe(String name, IItemStack output, IIngredient catalyst, AspectAmount[] aspects) {
        var definition = new JsonObject();
        definition.add("catalyst", Recipes.encode(IngredientCodecs.CODEC, catalyst.asVanillaIngredient()));
        definition.add("aspects", AspectAmount.definitions(aspects));
        definition.add("result", Recipes.encode(ItemStackCodecs.CODEC, output.getInternal()));
        add(name, definition);
    }

    @Override
    @ZenCodeType.Method
    public void addJsonRecipe(String name, MapData data) {
        add(name, json(data));
    }

    @ZenCodeType.Method
    public void replaceRecipe(ResourceLocation id, MapData data) {
        Recipes.replace(this, id, json(data));
    }

    @ZenCodeType.Method
    public String definition(ResourceLocation id) {
        var recipe = getRecipeList().get(id);
        if (recipe == null) throw new IllegalArgumentException("Unknown crucible recipe " + id);
        return Recipes.definition(recipe).toString();
    }

    private void add(String name, JsonObject definition) {
        if (!definition.has("type")) definition.addProperty("type", getBracketResourceLocation().toString());
        Recipes.add(this, name, definition);
    }

    private JsonObject json(MapData data) {
        JsonObject definition = JsonParser.parseString(data.accept(DataToJsonStringVisitor.INSTANCE)).getAsJsonObject();
        if (!definition.has("type")) definition.addProperty("type", getBracketResourceLocation().toString());
        return definition;
    }
}
