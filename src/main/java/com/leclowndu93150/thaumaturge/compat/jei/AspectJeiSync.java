package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.compat.jei.category.AspectCompositionCategory;
import com.leclowndu93150.thaumaturge.compat.jei.category.AspectFromStacksCategory;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class AspectJeiSync {
    private static @Nullable IJeiRuntime runtime;
    private static final Set<ResourceLocation> discoveredAspects = new HashSet<>();

    private AspectJeiSync() {}

    static void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        rebuildAspectStackPages();
        discoveredAspects.clear();
        syncDiscovered();
        updateCompositionVisibility(jeiRuntime);
    }

    static void onRuntimeUnavailable() {
        runtime = null;
        discoveredAspects.clear();
    }

    private static void updateCompositionVisibility(IJeiRuntime runtime) {
        IRecipeManager recipes = runtime.getRecipeManager();
        List<AspectCompositionCategory.Composition> hidden = new ArrayList<>();
        List<AspectCompositionCategory.Composition> shown = new ArrayList<>();
        recipes.createRecipeLookup(AspectCompositionCategory.RECIPE_TYPE)
                .includeHidden()
                .get()
                .forEach(row -> {
                    if (AspectKnowledgeAccess.of(row.result()).isCompositionRevealed()) {
                        shown.add(row);
                    } else {
                        hidden.add(row);
                    }
                });
        if (!hidden.isEmpty()) {
            recipes.hideRecipes(AspectCompositionCategory.RECIPE_TYPE, hidden);
        }
        if (!shown.isEmpty()) {
            recipes.unhideRecipes(AspectCompositionCategory.RECIPE_TYPE, shown);
        }
    }

    private static @Nullable ResourceLocation gatedAspectOf(ItemStack stack) {
        if (stack.is(TTItems.ESSENTIA_CRYSTAL.get())) {
            AspectInstance instance = ItemData.get(stack, TTDataComponents.CRYSTAL_ASPECT.get());
            return instance == null ? null : AspectPools.idOf(instance.aspect());
        }
        if (stack.is(TTItems.PHIAL.get())) {
            AspectList aspects = ItemData.get(stack, TTDataComponents.ASPECTS.get());
            if (aspects == null || aspects.isEmpty()) {
                return null;
            }
            return AspectPools.idOf(aspects.entries().get(0).aspect());
        }
        return null;
    }

    public static void rebuildAspectStackPages() {
        IJeiRuntime current = runtime;
        if (current == null) {
            return;
        }
        IRecipeManager recipes = current.getRecipeManager();
        List<AspectFromStacksCategory.Wrapper> stale = recipes.createRecipeLookup(AspectFromStacksCategory.RECIPE_TYPE)
                .includeHidden()
                .get()
                .toList();
        if (!stale.isEmpty()) {
            recipes.hideRecipes(AspectFromStacksCategory.RECIPE_TYPE, stale);
        }
        List<AspectFromStacksCategory.Wrapper> pages = ThaumaturgeJEIPlugin.aspectFromStacksPages(
                current.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK));
        if (!pages.isEmpty()) {
            recipes.addRecipes(AspectFromStacksCategory.RECIPE_TYPE, pages);
        }
    }

    public static void syncDiscovered() {
        IJeiRuntime current = runtime;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (current == null || player == null) {
            return;
        }
        IIngredientManager ingredients = current.getIngredientManager();
        Set<ResourceLocation> changedIds = new HashSet<>();
        player.level().registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .listElements()
                .forEach(ref -> {
                    ResourceLocation id = ref.key().location();
                    boolean discovered = AspectPools.isDiscovered(player, ref);
                    boolean known = discoveredAspects.contains(id);
                    if (discovered == known) {
                        return;
                    }
                    if (discovered) {
                        discoveredAspects.add(id);
                    } else {
                        discoveredAspects.remove(id);
                    }
                    changedIds.add(id);
                });
        if (changedIds.isEmpty()) {
            return;
        }
        refreshIngredients(ingredients, AspectIngredientType.INSTANCE,
                aspect -> changedIds.contains(AspectPools.idOf(aspect.aspect())));
        refreshIngredients(ingredients, VanillaTypes.ITEM_STACK,
                stack -> changedIds.contains(gatedAspectOf(stack)));
        updateCompositionVisibility(current);
    }

    private static <T> void refreshIngredients(
            IIngredientManager manager, IIngredientType<T> type, Predicate<T> isAffected) {
        List<T> affected = manager.getAllIngredients(type).stream().filter(isAffected).toList();
        if (!affected.isEmpty()) {
            manager.removeIngredientsAtRuntime(type, affected);
            manager.addIngredientsAtRuntime(type, affected);
        }
    }
}
