package com.leclowndu93150.thaumaturge.mixin.world.item.crafting;

import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
abstract class RecipeManagerMixin implements RecipeAccess.Owner {
    @Unique private RecipeAccess thaumaturge$recipeAccess;

    @Override
    public synchronized RecipeAccess thaumaturge$recipeAccess() {
        if (thaumaturge$recipeAccess == null) {
            thaumaturge$recipeAccess = new RecipeAccess((RecipeManager) (Object) this);
        }
        return thaumaturge$recipeAccess;
    }

    @Inject(method = {
            "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            "replaceRecipes"}, at = @At("RETURN"))
    private void thaumaturge$recipesReplaced(CallbackInfo ci) {
        if (thaumaturge$recipeAccess != null) thaumaturge$recipeAccess.invalidate();
    }

    @Redirect(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Lnet/minecraft/world/item/crafting/Recipe;"))
    private Recipe<?> thaumaturge$decodeWithRegistries(ResourceLocation id, JsonObject json, ICondition.IContext conditions) {
        // The three-argument fromJson overload is Forge-added and retains its name.
        return thaumaturge$recipeAccess().withRegistries(() -> RecipeManager.fromJson(id, json, conditions));
    }
}
