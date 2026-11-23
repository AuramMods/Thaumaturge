package com.leclowndu93150.thaumaturge.content.recipe.crucible;

import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;

import com.leclowndu93150.thaumaturge.serialization.IngredientCodecs;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.recipe.AbstractRecipe;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectRequirements;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGated;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CrucibleRecipe extends AbstractRecipe<CrucibleRecipeInput> implements ResearchGated {

    public static final MapCodec<CrucibleRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    IngredientCodecs.CODEC.fieldOf("catalyst").forGetter(r -> r.catalyst),
                    AspectRequirements.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.requirements),
                    ItemStackCodecs.CODEC.fieldOf("result").forGetter(r -> r.result),
                    Codecs.optionalFieldOf(ResearchGate.CODEC, "research").forGetter(r -> r.research))
            .apply(i, CrucibleRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> STREAM_CODEC = StreamCodec.composite(
            VanillaStreamCodecs.INGREDIENT,
            r -> r.catalyst,
            AspectRequirements.STREAM_CODEC,
            r -> r.requirements,
            VanillaStreamCodecs.ITEM_STACK,
            r -> r.result,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC),
            r -> r.research,
            CrucibleRecipe::new);

    public static final RecipeSerializer<CrucibleRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient catalyst;
    private final AspectRequirements requirements;
    private final ItemStack result;
    private final Optional<ResearchGate> research;

    public CrucibleRecipe(Ingredient catalyst, AspectList aspects, ItemStack result, Optional<ResearchGate> research) {
        this(catalyst, AspectRequirements.fixed(aspects), result, research);
    }

    public CrucibleRecipe(
            Ingredient catalyst, AspectRequirements requirements, ItemStack result, Optional<ResearchGate> research) {
        this.catalyst = catalyst;
        this.requirements = requirements;
        this.result = result;
        this.research = research;
    }

    @Override
    public boolean matches(CrucibleRecipeInput input, Level level) {
        return catalyst.test(input.catalyst()) && !input.availableAspects().isEmpty()
                && payment(input.availableAspects(), level.registryAccess()).isPresent();
    }

    public Optional<AspectList> payment(AspectList available, HolderLookup.Provider registries) {
        if (!requirements.hasTags()) {
            for (AspectInstance aspect : requirements.fixed().entries()) {
                if (available.amountOf(aspect.aspect()) < aspect.amount()) return Optional.empty();
            }
            return Optional.of(requirements.fixed());
        }
        var spent = requirements.resolve(registries).payment(available);
        return spent.totalAmount() == requirements.totalAmount() ? Optional.of(spent) : Optional.empty();
    }

    public AspectList removeMatching(AspectList old) {
        if (!requirements.hasTags()) return old.subtract(requirements.fixed());
        var registries = recipeRegistryLookup();
        if (registries == null) {
            throw new IllegalStateException("Tagged recipe requires the active world's registries");
        }
        return payment(old, registries).map(old::subtract).orElse(old);
    }

    @Override
    public ItemStack assemble(CrucibleRecipeInput crucibleRecipeInput, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<CrucibleRecipeInput>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<CrucibleRecipeInput>> getType() {
        return TTRecipeTypes.CRUCIBLE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public AspectList aspects() {
        return requirements.fixed();
    }

    public AspectRequirements requirements() {
        return requirements;
    }

    public Ingredient catalyst() {
        return catalyst;
    }

    public ItemStack rawResult() {
        return result;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }
}
