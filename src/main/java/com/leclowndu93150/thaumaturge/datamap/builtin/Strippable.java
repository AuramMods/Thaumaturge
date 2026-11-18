package com.leclowndu93150.thaumaturge.datamap.builtin;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record Strippable(Block strippedBlock) {
    public static final Codec<Strippable> STRIPPED_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec().xmap(Strippable::new, Strippable::strippedBlock);
    public static final Codec<Strippable> CODEC = Codecs.withAlternative(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stripped_block").xmap(Strippable::new, Strippable::strippedBlock).codec(), STRIPPED_BLOCK_CODEC);
}
