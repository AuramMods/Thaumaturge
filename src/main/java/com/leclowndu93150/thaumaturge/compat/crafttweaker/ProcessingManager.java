package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.ProcessingManager")
public interface ProcessingManager<T extends Recipe<?>> extends IRecipeManager<T> {
    @Override
    @ZenCodeType.Method
    default void addJsonRecipe(String name, MapData data) {
        Recipes.add(this, name, withType(JsonParser.parseString(data.accept(DataToJsonStringVisitor.INSTANCE)).getAsJsonObject()));
    }

    @ZenCodeType.Method
    default void addJson(String name, String json) {
        Recipes.add(this, name, withType(JsonParser.parseString(json).getAsJsonObject()));
    }

    @ZenCodeType.Method
    default void replaceRecipe(ResourceLocation id, MapData data) {
        Recipes.replace(this, id, withType(JsonParser.parseString(data.accept(DataToJsonStringVisitor.INSTANCE)).getAsJsonObject()));
    }

    @ZenCodeType.Method
    default String definition(ResourceLocation id) {
        var recipe = getRecipeList().get(id);
        if (recipe == null) throw new IllegalArgumentException("Unknown " + getBracketResourceLocation() + " recipe " + id);
        return Recipes.definition(recipe).toString();
    }

    private JsonObject withType(JsonObject definition) {
        if (!definition.has("type")) definition.addProperty("type", getBracketResourceLocation().toString());
        return definition;
    }
}
