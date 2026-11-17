package com.leclowndu93150.thaumaturge.content.research.share;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record ShareBinding(UUID player, String name, AspectPoolData discoveredAspects, PlayerKnowledge knowledge) {
    public static final Codec<ShareBinding> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    UUIDUtil.CODEC.fieldOf("player").forGetter(ShareBinding::player),
                    Codec.STRING.fieldOf("name").forGetter(ShareBinding::name),
                    AspectPoolData.CODEC
                            .codec()
                            .fieldOf("discovered_aspects")
                            .forGetter(ShareBinding::discoveredAspects),
                    PlayerKnowledge.CODEC.codec().fieldOf("knowledge").forGetter(ShareBinding::knowledge))
            .apply(builder, ShareBinding::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShareBinding> STREAM_CODEC = StreamCodec.composite(
            VanillaStreamCodecs.UUID,
            ShareBinding::player,
            ByteBufCodecs.STRING_UTF8,
            ShareBinding::name,
            AspectPoolData.STREAM_CODEC,
            ShareBinding::discoveredAspects,
            PlayerKnowledge.STREAM_CODEC,
            ShareBinding::knowledge,
            ShareBinding::new);
}
