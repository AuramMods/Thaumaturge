package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.RunicAugmentManager")
public enum RunicAugmentManager implements ProcessingManager<InfusionRunicAugmentRecipe> {
    INSTANCE;

    @Override
    public RecipeType<InfusionRunicAugmentRecipe> getRecipeType() {
        return TTRecipeTypes.RUNIC_AUGMENT.get();
    }

    @ZenCodeType.Method
    public void addRecipe(String name, IIngredient displayCatalyst, IIngredient[] components, IIngredient perLevel,
            AspectAmount[] aspects, @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = Recipes.recipe(InfusionRunicAugmentRecipe.SERIALIZER, research);
        json.add("components", Recipes.ingredients(components));
        json.add("display_catalyst", Recipes.ingredient(displayCatalyst));
        json.add("per_level", Recipes.ingredient(perLevel));
        json.add("aspects", AspectAmount.definitions(aspects));
        Recipes.add(this, name, json);
    }
}
