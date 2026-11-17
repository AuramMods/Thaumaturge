package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record BlueprintPart(BlueprintSource source, BlueprintTarget target, int priority) {
    public static final MapCodec<BlueprintPart> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    BlueprintSource.CODEC.fieldOf("source").forGetter(BlueprintPart::source),
                    Codecs.optionalFieldOf(BlueprintTarget.CODEC, "target", BlueprintTarget.Keep.INSTANCE)
                            .forGetter(BlueprintPart::target),
                    Codecs.optionalFieldOf(Codec.INT, "priority", 50).forGetter(BlueprintPart::priority))
            .apply(i, BlueprintPart::new));

    public static final Codec<BlueprintPart> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintPart> STREAM_CODEC = StreamCodec.composite(
            BlueprintSource.STREAM_CODEC,
            BlueprintPart::source,
            BlueprintTarget.STREAM_CODEC,
            BlueprintPart::target,
            ByteBufCodecs.VAR_INT,
            BlueprintPart::priority,
            BlueprintPart::new);
}
