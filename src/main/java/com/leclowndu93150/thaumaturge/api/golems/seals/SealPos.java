package com.leclowndu93150.thaumaturge.api.golems.seals;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;

/**
 * The placement of a seal: the block it is attached to and the face it sits on.
 *
 * @param pos  the block the seal is attached to
 * @param face the face of that block the seal occupies
 * @since 1.0.0
 */
public record SealPos(BlockPos pos, Direction face) {
    /** Codec for seal placements. */
    public static final Codec<SealPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(SealPos::pos),
                    Direction.CODEC.fieldOf("face").forGetter(SealPos::face))
            .apply(instance, SealPos::new));

    /** Stream codec for seal placements. */
    public static final StreamCodec<ByteBuf, SealPos> STREAM_CODEC = StreamCodec.composite(
            VanillaStreamCodecs.BLOCK_POS, SealPos::pos, VanillaStreamCodecs.DIRECTION, SealPos::face, SealPos::new);
}
