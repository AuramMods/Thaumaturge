package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.compat.jei.ThaumaturgeJEIPlugin;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.compat.jei.drawables.AlphaDrawable;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientRenderer;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.compat.jei.utils.ResearchUtils;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CrucibleCategory implements IRecipeCategory<CrucibleRecipe> {
    public static final RecipeType<CrucibleRecipe> RECIPE_TYPE =
            new RecipeType<>(TTRecipeTypes.CRUCIBLE.getId(), CrucibleRecipe.class);

    public static final int ASPECT_Y = 66;
    public static final int ASPECT_X = 66;
    public static final int SPACE = 22;
    private static final IDrawable background = new AlphaDrawable(
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            2,
            5,
            109,
            129,
            0,
            0,
            9,
            10);
    ;
    private static final IDrawable arrow = new AlphaDrawable(
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/gui/gui_researchbook_overlay.png"),
            199,
            168,
            26,
            26);

    private final IDrawable icon;

    public CrucibleCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(TTItems.CRUCIBLE.get()));
    }

    @Override
    public RecipeType<CrucibleRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.thaumaturge.recipe_type.crucible");
    }

    @Override
    public int getWidth() {
        return 129;
    }

    @Override
    public int getHeight() {
        return 129;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrucibleRecipe recipe, IFocusGroup focuses) {
        builder.addOutputSlot(55, 8).addItemStack(recipe.rawResult());
        builder.addInputSlot(2, 2).addIngredients(recipe.catalyst());

        if (recipe.requirements().hasTags()) {
            var demands = recipe.requirements().resolve(ThaumaturgeJEIPlugin.clientRegistryAccess()).demands();
            int center = (demands.size() * SPACE) / 2;
            for (int index = 0; index < demands.size(); index++) {
                var demand = demands.get(index);
                var choices = demand.choices().stream().map(aspect -> new AspectInstance(aspect, demand.amount()))
                        .toList();
                var slot = builder.addInputSlot(ASPECT_X - center + index * SPACE, ASPECT_Y)
                        .setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE)
                        .addIngredients(AspectIngredientType.INSTANCE, choices);
                demand.tag().ifPresent(tag -> slot.addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.literal("#" + tag.location() + " ×" + demand.amount()))));
                for (AspectInstance choice : choices) addUsages(builder, choice);
            }
            return;
        }
        int center = (recipe.aspects().size() * SPACE) / 2;
        int x = 0;

        for (AspectInstance instance : recipe.aspects().sortedByAmount()) {
            builder.addInputSlot(ASPECT_X - center + x * SPACE, ASPECT_Y)
                    .setCustomRenderer(AspectIngredientType.INSTANCE, AspectIngredientRenderer.INSTANCE)
                    .addIngredient(AspectIngredientType.INSTANCE, instance);

            addUsages(builder, instance);
            x += 1;
        }
    }

    private static void addUsages(IRecipeLayoutBuilder builder, AspectInstance instance) {
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                .addItemStack(PhialItem.makeFilled(instance.aspect()));
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                .addItemStack(EssentiaCrystalFactory.of(instance.aspect()));
    }

    @Override
    public void getTooltip(
            ITooltipBuilder tooltip,
            CrucibleRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY) {
        Optional<ResearchGate> gate = recipe.researchGate();
        boolean doesPassGate = recipe.doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate && mouseX > 22 && mouseX < 40 && mouseY > 14 && mouseY < 30) {
            tooltip.addAll(ResearchUtils.generateMissingResearchList(gate.get()));
        }
    }

    @Override
    public void draw(
            CrucibleRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {
        background.draw(guiGraphics);
        arrow.draw(guiGraphics, 16, 6);
        boolean doesPassGate = recipe.doesPassGate(Minecraft.getInstance().player);
        if (!doesPassGate) guiGraphics.renderItem(Items.BARRIER.getDefaultInstance(), 22, 14);
    }
}
