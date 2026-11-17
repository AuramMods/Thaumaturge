package com.leclowndu93150.thaumaturge.recipe;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** A holder-aware view owned by the native manager, with its reload lifetime. */
public final class RecipeAccess {
    public interface Owner {
        RecipeAccess thaumaturge$recipeAccess();
    }

    private final RecipeManager manager;
    private final Map<Recipe<?>, RecipeHolder<?>> holders = new IdentityHashMap<>();

    public RecipeAccess(RecipeManager manager) {
        this.manager = manager;
    }

    public static RecipeAccess of(RecipeManager manager) {
        return ((Owner) manager).thaumaturge$recipeAccess();
    }

    public synchronized void invalidate() {
        holders.clear();
    }

    @SuppressWarnings("unchecked")
    private synchronized <R extends Recipe<?>> RecipeHolder<R> holder(R recipe) {
        return (RecipeHolder<R>) holders.computeIfAbsent(recipe, value -> new RecipeHolder<>(value.getId(), value));
    }

    public <C extends Container, R extends Recipe<C>> List<RecipeHolder<R>> getAllRecipesFor(RecipeType<R> type) {
        return manager.getAllRecipesFor(type).stream().map(this::holder).toList();
    }

    public <C extends Container, R extends Recipe<C>> Optional<RecipeHolder<R>> getRecipeFor(
            RecipeType<R> type, C input, Level level) {
        return manager.getRecipeFor(type, input, level).map(this::holder);
    }

    public <C extends Container, R extends Recipe<C>> Optional<RecipeHolder<R>> getRecipeFor(
            RecipeType<R> type, C input, Level level, ResourceLocation hint) {
        return manager.getRecipeFor(type, input, level, hint).map(pair -> holder(pair.getSecond()));
    }

    public <C extends Container, R extends Recipe<C>> List<RecipeHolder<R>> getRecipesFor(
            RecipeType<R> type, C input, Level level) {
        return manager.getRecipesFor(type, input, level).stream().map(this::holder).toList();
    }

    public Optional<RecipeHolder<?>> byKey(ResourceLocation id) {
        return manager.byKey(id).<RecipeHolder<?>>map(this::holder);
    }

    public List<RecipeHolder<?>> getRecipes() {
        return manager.getRecipes().stream().<RecipeHolder<?>>map(this::holder).toList();
    }

    public Stream<ResourceLocation> getRecipeIds() {
        return manager.getRecipeIds();
    }

    public <C extends Container, R extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(
            RecipeType<R> type, C input, Level level) {
        return manager.getRemainingItemsFor(type, input, level);
    }
}
