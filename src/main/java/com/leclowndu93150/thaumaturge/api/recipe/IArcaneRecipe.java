package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.recipe.RegistryRecipe;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectRequirements;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.Nullable;

/**
 *
 * @since 1.0.0
 */
public interface IArcaneRecipe extends ResearchGated, RegistryRecipe<IArcaneCraftingInput> {
    int visCost();

    AspectList crystalCost();

    default AspectRequirements crystalRequirements() {
        return AspectRequirements.fixed(crystalCost());
    }

    default AspectRequirements.Resolved resolveCrystals(HolderLookup.@Nullable Provider registries) {
        return crystalRequirements().resolve(registries)
                .filterTagChoices(aspect -> TTAspects.PRIMALS.stream().anyMatch(aspect::is));
    }
}
