package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.mojang.serialization.Codec;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public final class EssentiaContentsComponent {
    public static final Codec<EssentiaList> CODEC = EssentiaList.CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, EssentiaList> STREAM_CODEC = EssentiaList.STREAM_CODEC;

    private EssentiaContentsComponent() {}
}
