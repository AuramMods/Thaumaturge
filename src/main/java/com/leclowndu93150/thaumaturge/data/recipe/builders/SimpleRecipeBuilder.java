package com.leclowndu93150.thaumaturge.data.recipe.builders;

import com.leclowndu93150.thaumaturge.data.recipe.RecipeDataOutput;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleRecipeBuilder implements RecipeBuilder {
    protected final ItemStack result;
    protected boolean showNotification = true;

    protected final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();
    protected final RecipeCategory category;

    public SimpleRecipeBuilder(ItemStack result, RecipeCategory category) {
        this.result = result;
        this.category = category;
    }

    @Override
    public SimpleRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public SimpleRecipeBuilder group(@Nullable String group) {
        return this;
    }

    public SimpleRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.getItem();
    }

    @Override
    public void save(Consumer<FinishedRecipe> output) {
        save(output, defaultId());
    }

    @Override
    public void save(Consumer<FinishedRecipe> output, String id) {
        save(output, ResourceLocation.parse(id));
    }

    protected ResourceLocation defaultId() {
        return RecipeBuilder.getDefaultRecipeId(getResult());
    }

    protected @Nullable RecipeDataOutput.Unlock buildAdvancement(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            return null;
        }
        Advancement.Builder builder = Advancement.Builder.advancement()
                .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(RequirementsStrategy.OR);
        this.criteria.forEach(builder::addCriterion);
        ResourceLocation advancementId = id.withPrefix("recipes/" + this.category.getFolderName() + "/");
        return new RecipeDataOutput.Unlock(advancementId, builder);
    }
}
