package com.leclowndu93150.thaumaturge.content.research.link;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record LinkBinding(UUID player, String name) {
    public static final Codec<LinkBinding> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    UUIDUtil.CODEC.fieldOf("player").forGetter(LinkBinding::player),
                    Codec.STRING.fieldOf("name").forGetter(LinkBinding::name))
            .apply(builder, LinkBinding::new));

    public static final StreamCodec<ByteBuf, LinkBinding> STREAM_CODEC = StreamCodec.composite(
            VanillaStreamCodecs.UUID, LinkBinding::player, ByteBufCodecs.STRING_UTF8, LinkBinding::name, LinkBinding::new);
}
