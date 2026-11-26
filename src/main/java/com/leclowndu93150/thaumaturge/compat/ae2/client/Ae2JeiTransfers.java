package com.leclowndu93150.thaumaturge.compat.ae2.client;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FillCraftingGridFromRecipePacket;
import com.leclowndu93150.thaumaturge.compat.ae2.ArcaneTerminalMenu;
import com.leclowndu93150.thaumaturge.compat.ae2.ThaumaturgeAe2;
import com.leclowndu93150.thaumaturge.compat.jei.category.ArcaneWorkbenchCategory;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public final class Ae2JeiTransfers {
    private Ae2JeiTransfers() {}

    public static void register(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new Transfer<>(RecipeTypes.CRAFTING), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(
                new Transfer<>(ArcaneWorkbenchCategory.RECIPE_TYPE), ArcaneWorkbenchCategory.RECIPE_TYPE);
    }

    private record Transfer<R>(RecipeType<R> getRecipeType) implements IRecipeTransferHandler<ArcaneTerminalMenu, R> {
        @Override
        public Class<ArcaneTerminalMenu> getContainerClass() {
            return ArcaneTerminalMenu.class;
        }

        @Override
        public Optional<MenuType<ArcaneTerminalMenu>> getMenuType() {
            return Optional.of(ThaumaturgeAe2.ARCANE_TERMINAL_MENU.get());
        }

        @Override
        public IRecipeTransferError transferRecipe(ArcaneTerminalMenu menu, R recipe, IRecipeSlotsView display,
                Player player, boolean maxTransfer, boolean doTransfer) {
            if (!doTransfer) return null;
            var templates = NonNullList.withSize(9, ItemStack.EMPTY);
            var slots = display.getSlotViews(RecipeIngredientRole.INPUT);
            var reserved = new Object2IntOpenHashMap<Object>();
            for (int i = 0; i < Math.min(9, slots.size()); i++) {
                var candidates = slots.get(i).getItemStacks().toList();
                if (candidates.isEmpty()) continue;
                ItemStack chosen = candidates.get(0);
                for (var stack : candidates) {
                    if (menu.hasIngredient(Ingredient.of(stack), reserved)) {
                        chosen = stack;
                        break;
                    }
                }
                templates.set(i, chosen.copyWithCount(1));
            }
            var recipeId = recipe instanceof Recipe<?> vanilla ? vanilla.getId() : null;
            NetworkHandler.instance().sendToServer(
                    new FillCraftingGridFromRecipePacket(recipeId, templates, Screen.hasControlDown()));
            return null;
        }
    }
}
