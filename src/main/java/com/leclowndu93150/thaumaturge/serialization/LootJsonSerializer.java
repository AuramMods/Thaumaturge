package com.leclowndu93150.thaumaturge.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.Serializer;

/** Keeps a loot type's field codec behind the native Gson dispatch. */
public final class LootJsonSerializer<T> implements Serializer<T> {
    private final Codec<T> codec;

    public LootJsonSerializer(MapCodec<T> fields) {
        this.codec = fields.codec();
    }

    @Override
    public void serialize(JsonObject json, T value, JsonSerializationContext context) {
        var encoded = codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow(false, error -> {
            throw new JsonParseException(error);
        });
        encoded.getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
    }

    @Override
    public T deserialize(JsonObject json, JsonDeserializationContext context) {
        return codec.parse(JsonOps.INSTANCE, json).getOrThrow(false, error -> {
            throw new JsonParseException(error);
        });
    }
}
