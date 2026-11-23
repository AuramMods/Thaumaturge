package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectRequirements;
import com.leclowndu93150.thaumaturge.compat.jei.ThaumaturgeJEIPlugin;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

final class AspectCostSlots {
    private AspectCostSlots() {}

    static void add(IRecipeLayoutBuilder builder, AspectRequirements requirements, int x, int y, int spacing) {
        if (!requirements.hasTags()) {
            var fixed = requirements.fixed().sortedByAmount();
            int left = x - fixed.size() * spacing / 2;
            for (int index = 0; index < fixed.size(); index++) {
                addSlot(builder, left + index * spacing, y, List.of(fixed.get(index)));
            }
            return;
        }

        var demands = requirements.resolve(ThaumaturgeJEIPlugin.clientRegistryAccess()).demands();
        int left = x - demands.size() * spacing / 2;
        for (int index = 0; index < demands.size(); index++) {
            var demand = demands.get(index);
            var choices = demand.choices().stream().map(aspect -> new AspectInstance(aspect, demand.amount())).toList();
            var slot = addSlot(builder, left + index * spacing, y, choices);
            demand.tag().ifPresent(tag -> slot.addRichTooltipCallback((view, tooltip) ->
                    tooltip.add(Component.literal("#" + tag.location() + " ×" + demand.amount()))));
        }
    }

    private static IRecipeSlotBuilder addSlot(
            IRecipeLayoutBuilder builder, int x, int y, List<AspectInstance> aspects) {
        for (AspectInstance instance : aspects) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                    .addItemStack(PhialItem.makeFilled(instance.aspect()));
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                    .addItemStack(EssentiaCrystalFactory.of(instance.aspect()));
        }
        return builder.addInputSlot(x, y)
                .setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE)
                .addIngredients(AspectIngredientType.INSTANCE, aspects);
    }
}
