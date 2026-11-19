package com.leclowndu93150.thaumaturge.recipe;

import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Stable recipe identity, including display variants whose value can share an ID. */
public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
    // Forge 1.20.1 uses RecipeManager IDs. Keep the later API's logical key for callers of our crafting APIs.
    private static final ResourceKey<Registry<Recipe<?>>> RECIPE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<?>> STREAM_CODEC = StreamCodec.of(
            (buffer, holder) -> {
                buffer.writeResourceLocation(holder.id());
                writeRecipe(buffer, holder.value());
            }, buffer -> {
                ResourceLocation id = buffer.readResourceLocation();
                int serializerId = buffer.readVarInt();
                RecipeSerializer<?> serializer = BuiltInRegistries.RECIPE_SERIALIZER.byId(serializerId);
                if (serializer == null) throw new DecoderException("Unknown recipe serializer ID " + serializerId);
                Recipe<?> recipe = serializer.fromNetwork(id, buffer);
                if (recipe == null) throw new DecoderException("Recipe serializer returned null for " + id);
                return new RecipeHolder<>(id, recipe);
            });

    public ResourceKey<Recipe<?>> key() {
        return ResourceKey.create(RECIPE_KEY, id);
    }

    @SuppressWarnings("unchecked")
    private static <R extends Recipe<?>> void writeRecipe(RegistryFriendlyByteBuf buffer, R recipe) {
        RecipeSerializer<R> serializer = (RecipeSerializer<R>) recipe.getSerializer();
        int serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getId(serializer);
        if (serializerId < 0) throw new EncoderException("Unregistered recipe serializer " + serializer);
        buffer.writeVarInt(serializerId);
        serializer.toNetwork(buffer, recipe);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof RecipeHolder<?> holder && id.equals(holder.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
