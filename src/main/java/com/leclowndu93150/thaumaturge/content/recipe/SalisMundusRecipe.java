package com.leclowndu93150.thaumaturge.content.recipe;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeSerializers;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import com.leclowndu93150.thaumaturge.recipe.CraftingInput;
import com.leclowndu93150.thaumaturge.recipe.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

public final class SalisMundusRecipe extends CustomRecipe {
    public static final SalisMundusRecipe INSTANCE = new SalisMundusRecipe();
    public static final MapCodec<SalisMundusRecipe> MAP_CODEC = MapCodec.unit(SalisMundusRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, SalisMundusRecipe> STREAM_CODEC =
            StreamCodec.unitFactory(SalisMundusRecipe::new);

    private SalisMundusRecipe() {
        super(CraftingBookCategory.MISC);
    }

    private static final int REQUIRED_CRYSTALS = 3;
    private static final Ingredient FLINT = Ingredient.of(Items.FLINT);
    private static final Ingredient REDSTONE = Ingredient.of(Tags.Items.DUSTS_REDSTONE);
    private static final Ingredient BOWL = Ingredient.of(Items.BOWL);
    private static final Ingredient CRYSTAL = Ingredient.of(TTItems.ESSENTIA_CRYSTAL.get());

    public static List<Ingredient> displayIngredients() {
        return List.of(FLINT, BOWL, REDSTONE, CRYSTAL, CRYSTAL, CRYSTAL);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean bowl = false;
        boolean flint = false;
        boolean redstone = false;
        Set<ResourceKey<IAspect>> crystals = new HashSet<>();
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (BOWL.test(stack)) {
                if (bowl) {
                    return false;
                }
                bowl = true;
            } else if (FLINT.test(stack)) {
                if (flint) {
                    return false;
                }
                flint = true;
            } else if (REDSTONE.test(stack)) {
                if (redstone) {
                    return false;
                }
                redstone = true;
            } else {
                AspectInstance aspect = ItemData.get(stack, TTDataComponents.CRYSTAL_ASPECT.get());
                if (!stack.is(TTItems.ESSENTIA_CRYSTAL.get()) || aspect == null) {
                    return false;
                }
                var key = aspect.aspect().unwrapKey().orElse(null);
                if (key == null || crystals.size() >= REQUIRED_CRYSTALS || !crystals.add(key)) {
                    return false;
                }
            }
        }
        return bowl && flint && redstone && crystals.size() == REQUIRED_CRYSTALS;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(TTItems.SALIS_MUNDUS.get());
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(TTItems.SALIS_MUNDUS.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 6;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < result.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (FLINT.test(stack) || BOWL.test(stack)) {
                result.set(slot, stack.copyWithCount(1));
            } else if (stack.getItem().hasCraftingRemainingItem(stack)) {
                result.set(slot, stack.getItem().getCraftingRemainingItem(stack));
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<SalisMundusRecipe> getSerializer() {
        return TTRecipeSerializers.SALIS_MUNDUS.get();
    }
}
