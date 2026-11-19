package com.leclowndu93150.thaumaturge.compat.apothicenchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.level.block.Block;

public record BlockEnchantingStats(List<Holder<Block>> blocks, EnchantingStats stats) {
    public static final Codec<BlockEnchantingStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RegistryFixedCodec.create(Registries.BLOCK).listOf()
                            .fieldOf("blocks")
                            .forGetter(BlockEnchantingStats::blocks),
                    EnchantingStats.CODEC.fieldOf("stats").forGetter(BlockEnchantingStats::stats))
            .apply(instance, BlockEnchantingStats::new));
}
