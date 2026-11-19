package com.leclowndu93150.thaumaturge.content.eldritch.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.Test;

class EldritchInsetRuntimeTest {
    @Test
    void nativeStateCallbackUsesTheConfiguredExperienceRangeAndHonorsSilkTouch() {
        var crystal = registerInset("crystal", UniformInt.of(1, 4));
        var crust = registerInset("crust", ConstantInt.of(0));
        var level = mock(LevelReader.class);
        var actual = RandomSource.create(721);
        var expected = RandomSource.create(721);
        var pos = new BlockPos(-8, 44, 32);

        assertEquals(0, crust.getExpDrop(level, actual, pos, 0, 0));
        assertEquals(0, crystal.getExpDrop(level, actual, pos, 0, 1));
        expected.nextInt(4);
        for (int i = 0; i < 24; i++) {
            assertEquals(1 + expected.nextInt(4), crystal.getExpDrop(level, actual, pos, 3, 0));
        }
    }

    private static BlockState registerInset(String id, IntProvider experience) {
        var forge = (ForgeRegistry<?>) ForgeRegistries.BLOCKS;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.BLOCK;
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            var block = new BlockEldritchInset(experience, BlockBehaviour.Properties.of());
            ForgeRegistries.BLOCKS.register(ResourceLocation.fromNamespaceAndPath("port_eldritch_inset", id), block);
            return block.defaultBlockState();
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }
}
