package com.leclowndu93150.thaumaturge.content.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.leclowndu93150.thaumaturge.recipe.AbstractRecipe;
import com.leclowndu93150.thaumaturge.recipe.IdentifiedRecipe;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class SimpleRecipeSerializer<T extends Recipe<?> & IdentifiedRecipe> implements RecipeSerializer<T> {
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public SimpleRecipeSerializer(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    public MapCodec<T> codec() {
        return codec;
    }

    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
    }

    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
        var registries = RegistryContext.resolve(null);
        return RegistryContext.withRegistries(registries, () -> {
            var decoded = codec.codec().parse(RegistryOps.create(JsonOps.INSTANCE, registries), json);
            T value = decoded.result().orElseThrow(() -> new JsonParseException("Invalid recipe " + id + ": "
                    + decoded.error().map(error -> error.message()).orElse("unknown codec error")));
            return identify(value, id, registries);
        });
    }

    @Override
    public T fromNetwork(ResourceLocation id, FriendlyByteBuf source) {
        RegistryFriendlyByteBuf buffer = contextual(source, null);
        return RegistryContext.withRegistries(buffer.registryLookup(), () -> {
            T value = streamCodec.decode(buffer);
            return identify(value, id, buffer.registryLookup());
        });
    }

    @Override
    public void toNetwork(FriendlyByteBuf source, T value) {
        HolderLookup.Provider owner = value instanceof AbstractRecipe<?> recipe ? recipe.recipeRegistryLookup() : null;
        RegistryFriendlyByteBuf buffer = contextual(source, owner);
        RegistryContext.withRegistries(buffer.registryLookup(), () -> {
            streamCodec.encode(buffer, value);
            return null;
        });
    }

    private T identify(T value, ResourceLocation id, HolderLookup.Provider registries) {
        if (value instanceof AbstractRecipe<?> recipe) recipe.bindRecipeId(id, registries);
        else value.bindRecipeId(id);
        return value;
    }

    private static RegistryFriendlyByteBuf contextual(FriendlyByteBuf source, HolderLookup.Provider owner) {
        return source instanceof RegistryFriendlyByteBuf registry ? registry
                : new RegistryFriendlyByteBuf(source, owner != null ? owner : RegistryContext.resolve(null));
    }
}
