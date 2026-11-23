package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageRules.Requirement;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import com.leclowndu93150.thaumaturge.recipe.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

public final class InfusionRecipeMatcher {
    private InfusionRecipeMatcher() {}

    public static @Nullable Match find(ServerLevel level, Player player, InfusionInput input) {
        Match locked = null;
        for (RecipeType<? extends InfusionJobRecipe> type : List.of(
                TTRecipeTypes.INFUSION.get(),
                TTRecipeTypes.INFUSION_ENCHANTMENT.get(),
                TTRecipeTypes.RUNIC_AUGMENT.get())) {
            Match match = scan(level, player, input, type);
            if (match != null && !match.locked()) {
                return match;
            }
            if (match != null && (locked == null || locked.stageDenial() == null && match.stageDenial() != null)) {
                locked = match;
            }
        }
        return locked;
    }

    private static <T extends InfusionJobRecipe> @Nullable Match scan(
            ServerLevel level, Player player, InfusionInput input, RecipeType<T> type) {
        Match locked = null;
        for (RecipeHolder<T> holder : RecipeAccess.of(level.getRecipeManager()).getAllRecipesFor(type)) {
            if (!holder.value().matches(input, level)) {
                continue;
            }
            if (!holder.value().doesPassGate(player)) {
                if (locked == null) locked = new Match(holder, true, null);
                continue;
            }
            var denial = StageRules.denial(level, StageTarget.recipe(StageTarget.INFUSION, holder.id()), player);
            if (denial == null) {
                return new Match(holder, false, null);
            }
            if (locked == null || locked.stageDenial() == null) locked = new Match(holder, false, denial);
        }
        return locked;
    }

    public record Match(RecipeHolder<? extends InfusionJobRecipe> holder, boolean researchLocked,
                        @Nullable Requirement stageDenial) {

        public Match(RecipeHolder<? extends InfusionJobRecipe> holder, boolean locked) {
            this(holder, locked, null);
        }

        public boolean locked() {
            return researchLocked || stageDenial != null;
        }

        public InfusionJobRecipe recipe() {
            return holder.value();
        }
    }
}
