package com.leclowndu93150.thaumaturge.content.recipe;

import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.thaumaturge.recipe.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ThaumaturgeCraftingManager {

    public static @Nullable ArcaneCraftingRecipe findMatchingArcaneRecipe(
            Level level, ArcaneCraftingInput input, Player player) {
        return RecipeAccess.of(level.getRecipeManager()).getAllRecipesFor(TTRecipeTypes.ARCANE.get()).stream()
                .filter(r -> r.value().matches(input, level))
                .filter(r -> r.value().doesPassGate(player))
                .filter(r -> !(level instanceof ServerLevel server)
                        || StageRules.allows(server, StageTarget.recipe(StageTarget.ARCANE_WORKBENCH, r.id()), player))
                .findFirst()
                .map(RecipeHolder::value)
                .orElse(null);
    }

    public static @Nullable CrucibleRecipe findMatchingCrucibleRecipe(
            ServerLevel level, Player player, AspectList aspects, ItemStack lastDrop) {
        return matchCrucible(level, player, aspects, lastDrop).recipe();
    }

    public static CrucibleMatch matchCrucible(
            ServerLevel level, Player player, AspectList aspects, ItemStack lastDrop) {
        int highest = 0;
        CrucibleRecipe out = null;
        AspectList paid = AspectList.EMPTY;
        StageRules.Requirement denial = null;

        CrucibleRecipeInput input = new CrucibleRecipeInput(lastDrop, aspects);
        for (RecipeHolder<CrucibleRecipe> holder :
                RecipeAccess.of(level.getRecipeManager()).getAllRecipesFor(TTRecipeTypes.CRUCIBLE.get())) {
            CrucibleRecipe recipe = holder.value();

            if (player != null && recipe.matches(input, level)) {
                var payment = recipe.payment(aspects, level.registryAccess());
                if (payment.isEmpty()) continue;
                var restriction = StageRules.denial(level, StageTarget.recipe(StageTarget.CRUCIBLE, holder.id()), player);
                if (restriction != null) {
                    denial = restriction;
                    continue;
                }
                if (!recipe.doesPassGate(player)) {
                    continue;
                }
                int result = recipe.requirements().totalAmount();
                if (result > highest) {
                    highest = result;
                    out = recipe;
                    paid = payment.get();
                }
            }
        }

        return new CrucibleMatch(out, paid, out == null ? denial : null);
    }

    public record CrucibleMatch(@Nullable CrucibleRecipe recipe, AspectList payment,
                                @Nullable StageRules.Requirement denial) {}
}
