package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticleType;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

public record RiftShardParticleOptions(float scale) implements TTParticleOptions {

    public static final MapCodec<RiftShardParticleOptions> CODEC = Codec.FLOAT
            .xmap(RiftShardParticleOptions::new, RiftShardParticleOptions::scale)
            .fieldOf("scale");

    public static final StreamCodec<ByteBuf, RiftShardParticleOptions> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(RiftShardParticleOptions::new, RiftShardParticleOptions::scale);

    @Override
    public TTParticleType<RiftShardParticleOptions> getType() {
        return TTParticles.RIFT_SHARD.get();
    }
}
