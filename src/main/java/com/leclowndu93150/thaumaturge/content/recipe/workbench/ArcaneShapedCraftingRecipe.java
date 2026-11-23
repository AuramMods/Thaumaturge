package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.google.common.annotations.VisibleForTesting;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectRequirements;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ArcaneShapedCraftingRecipe extends ArcaneCraftingRecipe {

    public static final MapCodec<ArcaneShapedCraftingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                    Codecs.optionalFieldOf(Codec.STRING, "group", "").forGetter((o) -> o.group),
                    ExtraCodecs.POSITIVE_INT.fieldOf("vis").forGetter(o -> o.vis),
                    Codecs.optionalFieldOf(ResearchGate.CODEC, "research").forGetter(o -> o.gate),
                    ArcaneCraftingRecipe.CRYSTAL_REQUIREMENTS
                            .fieldOf("crystals")
                            .forGetter(o -> o.requirements),
                    ArcaneShapedRecipePattern.MAP_CODEC.forGetter((o) -> o.pattern),
                    ItemStackCodecs.CODEC.fieldOf("result").forGetter((o) -> o.result))
            .apply(i, ArcaneShapedCraftingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneShapedCraftingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    (o) -> o.group,
                    ByteBufCodecs.VAR_INT,
                    (o) -> o.vis,
                    ByteBufCodecs.optional(ResearchGate.STREAM_CODEC),
                    o -> o.gate,
                    AspectRequirements.STREAM_CODEC,
                    o -> o.requirements,
                    ArcaneShapedRecipePattern.STREAM_CODEC,
                    (o) -> o.pattern,
                    VanillaStreamCodecs.ITEM_STACK,
                    (o) -> o.result,
                    ArcaneShapedCraftingRecipe::new);

    public static final RecipeSerializer<ArcaneShapedCraftingRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    public final ArcaneShapedRecipePattern pattern;
    private final ItemStack result;

    public ArcaneShapedCraftingRecipe(
            String group,
            int vis,
            Optional<ResearchGate> researchGate,
            AspectList aspects,
            ArcaneShapedRecipePattern pattern,
            ItemStack result) {
        this(group, vis, researchGate, AspectRequirements.fixed(aspects), pattern, result);
    }

    public ArcaneShapedCraftingRecipe(
            String group,
            int vis,
            Optional<ResearchGate> researchGate,
            AspectRequirements requirements,
            ArcaneShapedRecipePattern pattern,
            ItemStack result) {
        super(group, vis, researchGate, requirements);
        this.pattern = pattern;
        this.result = result;
    }

    @Override
    public RecipeSerializer<? extends ArcaneCraftingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @VisibleForTesting
    public List<Optional<Ingredient>> optionalIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (Optional<Ingredient> ingredient : this.pattern.ingredients()) {
            list.add(ingredient.orElse(Ingredient.EMPTY));
        }
        return list;
    }

    public ItemStack result() {
        return this.result;
    }

    @Override
    public boolean matches(IArcaneCraftingInput input, Level level) {
        return this.pattern.matches(input);
    }

    @Override
    public ItemStack assemble(IArcaneCraftingInput input, HolderLookup.Provider registries) {
        return assembleResult(this.result, input);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.pattern.width() && height >= this.pattern.height();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }
}
