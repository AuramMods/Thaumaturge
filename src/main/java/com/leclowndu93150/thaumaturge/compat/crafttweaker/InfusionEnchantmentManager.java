package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionEnchantmentRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.InfusionEnchantmentManager")
public enum InfusionEnchantmentManager implements ProcessingManager<InfusionEnchantmentRecipe> {
    INSTANCE;

    @Override
    public RecipeType<InfusionEnchantmentRecipe> getRecipeType() {
        return TTRecipeTypes.INFUSION_ENCHANTMENT.get();
    }

    @ZenCodeType.Method
    public void addRecipe(String name, String enchantment, IIngredient displayCatalyst, IIngredient[] components,
            AspectAmount[] aspects, @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = Recipes.recipe(InfusionEnchantmentRecipe.SERIALIZER, research);
        json.addProperty("enchantment", enchantment);
        json.add("components", Recipes.ingredients(components));
        json.add("display_catalyst", Recipes.ingredient(displayCatalyst));
        json.add("aspects", AspectAmount.definitions(aspects));
        Recipes.add(this, name, json);
    }
}
