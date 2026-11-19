package com.leclowndu93150.thaumaturge.content.recipe.label;

import com.leclowndu93150.thaumaturge.compat.Java17;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import com.leclowndu93150.thaumaturge.recipe.CraftingInput;
import com.leclowndu93150.thaumaturge.recipe.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class LabelFilterRecipe extends CustomRecipe {
    public static final LabelFilterRecipe INSTANCE = new LabelFilterRecipe();
    public static final MapCodec<LabelFilterRecipe> MAP_CODEC = MapCodec.unit(LabelFilterRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, LabelFilterRecipe> STREAM_CODEC =
            StreamCodec.unitFactory(LabelFilterRecipe::new);
    public static final RecipeSerializer<LabelFilterRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private LabelFilterRecipe() {
        super(CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return checkAndGetAspectFromInput(input) != null;
    }

    private @Nullable Holder<IAspect> checkAndGetAspectFromInput(CraftingInput input) {
        boolean hasLabel = false;
        Holder<IAspect> aspect = null;
        for (ItemStack stack : input.items()) {
            if (stack.is(TTItems.LABEL.get())) {
                if (hasLabel) return null;
                hasLabel = true;
                continue;
            }

            if (stack.is(TTItems.PHIAL.get())) {
                if (aspect != null) return null;
                IItemEssentia essentia = stack.getCapability(EssentiaCapabilities.CONTAINER).orElse(null);
                if (essentia == null || essentia.getAspects().isEmpty()) return null;
                aspect = Java17.first(essentia.getAspects().entries()).aspect();
            } else if (!stack.isEmpty()) {
                return null;
            }
        }
        if (!hasLabel) return null;
        return aspect;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Holder<IAspect> aspect = checkAndGetAspectFromInput(input);
        if (aspect == null) return ItemStack.EMPTY;
        return LabelItem.withAspect(aspect);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int slot = 0; slot < result.size(); ++slot) {
            ItemStack item = input.getItem(slot);
            if (item.is(TTItems.PHIAL.get())) result.set(slot, item.copyWithCount(1));
            else result.set(slot, item.hasCraftingRemainingItem() ? item.getCraftingRemainingItem() : ItemStack.EMPTY);
        }

        return result;
    }
}
