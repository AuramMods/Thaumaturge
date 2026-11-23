package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.plugin.CraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ICraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.IRecipeComponentRegistrationHandler;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.google.gson.reflect.TypeToken;
import com.leclowndu93150.thaumaturge.TTIds;

@CraftTweakerPlugin("thaumaturge:recipes")
public final class RecipeComponents implements ICraftTweakerPlugin {
    static final IRecipeComponent<String> DEFINITION =
            IRecipeComponent.simple(TTIds.rl("recipe/definition"), TypeToken.get(String.class), String::equals);

    @Override
    public void registerRecipeComponents(IRecipeComponentRegistrationHandler handler) {
        handler.registerRecipeComponent(DEFINITION);
    }
}
