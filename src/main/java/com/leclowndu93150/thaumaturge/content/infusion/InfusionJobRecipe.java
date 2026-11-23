package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.recipe.RegistryRecipe;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectRequirements;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public interface InfusionJobRecipe extends IInfusionRecipe, RegistryRecipe<InfusionInput> {

    default @Nullable List<ItemStack> jobComponents(InfusionInput input) {
        return matchComponents(input.components());
    }

    default AspectList jobEssentia(InfusionInput input) {
        return aspects();
    }

    default AspectRequirements jobAspectRequirements(InfusionInput input) {
        var requirements = aspectRequirements();
        return requirements.hasTags() ? requirements : AspectRequirements.fixed(jobEssentia(input));
    }

    default int jobInstability(InfusionInput input) {
        return instability();
    }

    default ItemStack jobResult(InfusionInput input, RandomSource random, HolderLookup.Provider registries) {
        return assemble(input, registries);
    }

    default boolean exactResult() {
        return true;
    }
}
