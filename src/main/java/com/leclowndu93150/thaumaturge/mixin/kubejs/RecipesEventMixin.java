package com.leclowndu93150.thaumaturge.mixin.kubejs;

import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "dev.latvian.mods.kubejs.recipe.RecipesEventJS", remap = false)
abstract class RecipesEventMixin {
    @Unique private RecipeAccess thaumaturge$recipes;

    @WrapMethod(method = "post")
    private void thaumaturge$applyWithRegistries(RecipeManager manager, Map<ResourceLocation, JsonElement> source,
            Operation<Void> original) {
        RecipeAccess recipes = RecipeAccess.of(manager);
        RecipeAccess previous = thaumaturge$recipes;
        thaumaturge$recipes = recipes;
        try {
            recipes.withRegistries(() -> original.call(manager, source));
        } finally {
            thaumaturge$recipes = previous;
        }
    }

    @ModifyArg(method = {"lambda$post$6", "lambda$post$7"},
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private Function<?, ?> thaumaturge$convertWithRegistries(Function<Object, ?> convert) {
        // The original and added recipe streams run their conversions on pooled workers.
        RecipeAccess recipes = thaumaturge$recipes;
        return recipe -> recipes.withRegistries(() -> convert.apply(recipe));
    }
}
