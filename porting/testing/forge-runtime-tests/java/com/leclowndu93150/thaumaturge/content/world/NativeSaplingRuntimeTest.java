package com.leclowndu93150.thaumaturge.content.world;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.world.tree.BlockSaplingTT;
import com.leclowndu93150.thaumaturge.content.world.tree.TTTreeGrowers;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NativeSaplingRuntimeTest {
    private static BlockSaplingTT greatwood;
    private static BlockSaplingTT silverwood;

    @BeforeAll
    static void registerSaplingsFromTheSourceBlockSchema() {
        var nativeBlocks = (MappedRegistry<?>) BuiltInRegistries.BLOCK;
        var forgeBlocks = (ForgeRegistry<?>) ForgeRegistries.BLOCKS;
        nativeBlocks.unfreeze();
        forgeBlocks.unfreeze();
        try {
            greatwood = registerSapling("greatwood");
            silverwood = registerSapling("silverwood");
        } finally {
            forgeBlocks.freeze();
            nativeBlocks.freeze();
        }
    }

    @Test
    void saplingCodecAndBonemealKeepTheTreeIdsAndQuarterChance() {
        assertTree(greatwood, "greatwood");
        assertTree(silverwood, "silverwood");
        var level = mock(ServerLevel.class);
        var random = mock(RandomSource.class);
        var state = greatwood.defaultBlockState();
        assertEquals(0, state.getValue(SaplingBlock.STAGE));
        when(random.nextFloat()).thenReturn(Math.nextDown(0.25F), 0.25F);
        assertTrue(greatwood.isBonemealSuccess(level, random, BlockPos.ZERO, state));
        assertFalse(greatwood.isBonemealSuccess(level, random, BlockPos.ZERO, state));
        greatwood.performBonemeal(level, random, BlockPos.ZERO, state);
        verify(level).setBlock(BlockPos.ZERO, state.setValue(SaplingBlock.STAGE, 1), 4);
    }

    @Test
    void greatwoodRequiresFourSaplingsAndRestoresThemWhenPlacementFails() {
        var level = mock(ServerLevel.class);
        var generator = attachChunkGenerator(level);
        var random = mock(RandomSource.class);
        var origin = new BlockPos(8, 64, 11);
        var clicked = origin.east().south();
        var grown = greatwood.defaultBlockState().setValue(SaplingBlock.STAGE, 1);
        var cells = Set.of(origin, origin.east(), origin.south(), clicked);
        var air = Blocks.AIR.defaultBlockState();

        when(level.getBlockState(any())).thenAnswer(call -> clicked.equals(call.getArgument(0)) ? grown : air);
        greatwood.performBonemeal(level, random, clicked, grown);
        verify(level, never()).setBlock(any(), any(), anyInt());
        verify(random).nextFloat();

        when(level.getBlockState(any())).thenAnswer(call -> cells.contains(call.getArgument(0)) ? grown : air);
        ConfiguredFeature<?, ?> feature = mock(ConfiguredFeature.class);
        attachFeature(level, TTTreeGrowers.GREATWOOD_TREE_GROWN, feature);
        greatwood.performBonemeal(level, random, clicked, grown);
        verify(feature).place(level, generator, random, origin);
        for (var pos : cells) {
            var order = inOrder(level);
            order.verify(level).setBlock(pos, air, 4);
            order.verify(level).setBlock(pos, grown, 4);
        }
        verify(random).nextFloat();
    }

    @Test
    void silverwoodGrowsFromOneSaplingAndAcceptsAForgeFeatureReplacement() {
        var level = mock(ServerLevel.class);
        var generator = attachChunkGenerator(level);
        var random = mock(RandomSource.class);
        var pos = new BlockPos(6, 70, -4);
        var grown = silverwood.defaultBlockState().setValue(SaplingBlock.STAGE, 1);
        var air = Blocks.AIR.defaultBlockState();
        when(level.getBlockState(any())).thenReturn(air);
        when(level.getFluidState(pos)).thenReturn(Fluids.EMPTY.defaultFluidState());
        ConfiguredFeature<?, ?> original = mock(ConfiguredFeature.class);
        ConfiguredFeature<?, ?> replacement = mock(ConfiguredFeature.class);
        attachFeature(level, TTTreeGrowers.SILVERWOOD_TREE_GROWN, original);
        when(replacement.place(level, generator, random, pos)).thenReturn(true);

        Consumer<SaplingGrowTreeEvent> replace = event -> {
            if (event.getLevel() != level) return;
            assertEquals(pos, event.getPos());
            assertSame(original, event.getFeature().value());
            event.setFeature(Holder.direct(replacement));
        };
        MinecraftForge.EVENT_BUS.addListener(replace);
        try {
            silverwood.performBonemeal(level, random, pos, grown);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(replace);
        }
        verify(original, never()).place(any(), any(), any(), any());
        verify(replacement).place(level, generator, random, pos);
        verify(level).setBlock(pos, air, 4);
        verify(level, never()).setBlock(pos, grown, 4);
        verify(random).nextFloat();
    }

    private static BlockSaplingTT registerSapling(String tree) {
        var encoded = JsonParser.parseString("{\"tree\":\"thaumaturge:" + tree + "\",\"properties\":{}}");
        var sapling = BlockSaplingTT.CODEC.codec().parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        ForgeRegistries.BLOCKS.register(ResourceLocation.fromNamespaceAndPath("port_sapling", tree), sapling);
        return sapling;
    }

    private static void assertTree(BlockSaplingTT sapling, String tree) {
        var encoded = BlockSaplingTT.CODEC.codec().encodeStart(JsonOps.INSTANCE, sapling).result().orElseThrow();
        assertEquals("thaumaturge:" + tree, encoded.getAsJsonObject().get("tree").getAsString());
    }

    private static ChunkGenerator attachChunkGenerator(ServerLevel level) {
        var chunkSource = mock(ServerChunkCache.class);
        var generator = mock(ChunkGenerator.class);
        when(chunkSource.getGenerator()).thenReturn(generator);
        when(level.getChunkSource()).thenReturn(chunkSource);
        return generator;
    }

    private static void attachFeature(ServerLevel level, ResourceKey<ConfiguredFeature<?, ?>> key,
            ConfiguredFeature<?, ?> feature) {
        var features = new MappedRegistry<ConfiguredFeature<?, ?>>(Registries.CONFIGURED_FEATURE, Lifecycle.stable());
        features.register(key, feature, Lifecycle.stable());
        when(level.registryAccess()).thenReturn(new RegistryAccess.ImmutableRegistryAccess(List.of(features)).freeze());
    }
}
