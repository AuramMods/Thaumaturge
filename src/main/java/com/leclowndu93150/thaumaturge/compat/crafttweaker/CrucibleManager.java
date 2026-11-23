package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.CrucibleManager")
public enum CrucibleManager implements ProcessingManager<CrucibleRecipe> {
    INSTANCE;

    @Override
    public RecipeType<CrucibleRecipe> getRecipeType() {
        return TTRecipeTypes.CRUCIBLE.get();
    }

    @ZenCodeType.Method
    public void addRecipe(String name, IItemStack output, IIngredient catalyst, AspectAmount[] aspects,
            @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var definition = Recipes.recipe(CrucibleRecipe.SERIALIZER, research);
        definition.add("catalyst", Recipes.ingredient(catalyst));
        definition.add("aspects", AspectAmount.definitions(aspects));
        definition.add("result", Recipes.stack(output));
        Recipes.add(this, name, definition);
    }
}
