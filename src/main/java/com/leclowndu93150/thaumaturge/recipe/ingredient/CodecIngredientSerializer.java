package com.leclowndu93150.thaumaturge.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataContext;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

/** Native Forge serializer with a codec path that retains the caller's actual ops/lookup. */
public final class CodecIngredientSerializer<I extends Ingredient> implements IIngredientSerializer<I> {
    private static final Map<ResourceLocation, CodecIngredientSerializer<?>> REGISTERED = new ConcurrentHashMap<>();
    private final MapCodec<I> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, I> stream;

    public CodecIngredientSerializer(MapCodec<I> codec) {
        this.codec = codec;
        stream = ByteBufCodecs.fromCodecWithRegistries(codec.codec());
    }

    public static void register(ResourceLocation id, CodecIngredientSerializer<?> serializer) {
        CraftingHelper.register(id, serializer);
        REGISTERED.put(id, serializer);
    }

    public static CodecIngredientSerializer<?> registered(ResourceLocation id) { return REGISTERED.get(id); }

    public <T> DataResult<I> decode(DynamicOps<T> ops, T input) {
        return ItemDataContext.withOps(ops, () -> codec.codec().parse(ops, input));
    }

    @Override public I parse(JsonObject json) {
        var registries = RegistryContext.resolve(null);
        return RegistryContext.withRegistries(registries, () -> {
            var decoded = decode(RegistryOps.create(JsonOps.INSTANCE, registries), json);
            return decoded.result().orElseThrow(() -> new JsonParseException("Invalid ingredient " + CraftingHelper.getID(this)
                    + ": " + decoded.error().orElseThrow().message()));
        });
    }

    @Override public I parse(FriendlyByteBuf source) {
        var buffer = contextual(source);
        return RegistryContext.withRegistries(buffer.registryLookup(), () -> stream.decode(buffer));
    }

    @Override public void write(FriendlyByteBuf source, I ingredient) {
        var buffer = contextual(source);
        RegistryContext.withRegistries(buffer.registryLookup(), () -> { stream.encode(buffer, ingredient); return null; });
    }

    @SuppressWarnings("unchecked")
    public <T> DataResult<T> encodeTagged(Ingredient ingredient, DynamicOps<T> ops, T prefix) {
        ResourceLocation id = CraftingHelper.getID(this);
        if (id == null) return DataResult.error(() -> "Unregistered ingredient serializer");
        return ItemDataContext.withOps(ops, () -> {
            var builder = ops.mapBuilder().add("type", ops.createString(id.toString()));
            return codec.encode((I) ingredient, ops, builder).build(prefix);
        });
    }

    public JsonElement toJson(I ingredient) {
        var registries = RegistryContext.resolve(null);
        return RegistryContext.withRegistries(registries, () -> {
            var encoded = encodeTagged(ingredient, RegistryOps.create(JsonOps.INSTANCE, registries), JsonOps.INSTANCE.empty());
            return encoded.result().orElseThrow(() -> new JsonParseException("Cannot encode ingredient "
                    + CraftingHelper.getID(this) + ": " + encoded.error().orElseThrow().message()));
        });
    }

    private static RegistryFriendlyByteBuf contextual(FriendlyByteBuf source) {
        return source instanceof RegistryFriendlyByteBuf buffer ? buffer
                : new RegistryFriendlyByteBuf(source, RegistryContext.resolve(null));
    }
}
