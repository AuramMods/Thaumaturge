package com.leclowndu93150.thaumaturge.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** The baseline's block-construction schema, retained on Minecraft 1.20.1. */
public final class BlockCodecs {
    // Vanilla 1.21.1 itself uses a unit codec with a fresh Properties.of() on
    // decode. It does not serialize hardness, sound, predicates or other fields.
    public static final Codec<BlockBehaviour.Properties> PROPERTIES = Codec.unit(BlockBehaviour.Properties::of);

    private BlockCodecs() {}

    public static <B extends Block> RecordCodecBuilder<B, BlockBehaviour.Properties> propertiesCodec() {
        return PROPERTIES.fieldOf("properties").forGetter(BlockBehaviour.Properties::copy);
    }

    public static <B extends Block> MapCodec<B> simpleCodec(Function<BlockBehaviour.Properties, B> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(BlockCodecs.<B>propertiesCodec())
                .apply(instance, factory));
    }
}
