package com.leclowndu93150.thaumaturge.content.blockentity;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NativeBlockBreakRuntimeTest {
    private static Block block;

    @BeforeAll
    static void registerFixtureBlock() {
        var forge = (ForgeRegistry<?>) ForgeRegistries.BLOCKS;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.BLOCK;
        boolean locked = forge.isLocked();
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            block = new Block(BlockBehaviour.Properties.copy(Blocks.STONE)) {
                @Override
                public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
                    level.getBlockEntity(pos);
                    super.playerWillDestroy(level, pos, state, player);
                }
            };
            ForgeRegistries.BLOCKS.register(ResourceLocation.fromNamespaceAndPath("port_block_break", "fixture"), block);
        } finally {
            if (locked) forge.freeze();
            vanilla.freeze();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void forgeCallsThePreBreakHookBeforeReplacingTheBlockWithItsFluid(boolean canHarvest) {
        var level = mock(ServerLevel.class);
        var player = mock(Player.class);
        var pos = new BlockPos(7, 64, 14);
        var state = block.defaultBlockState();
        var fluid = Fluids.WATER.defaultFluidState();
        var replacement = fluid.createLegacyBlock();
        when(level.setBlock(pos, replacement, Block.UPDATE_ALL)).thenReturn(true);

        assertTrue(state.onDestroyedByPlayer(level, pos, player, canHarvest, fluid));

        var order = inOrder(level);
        order.verify(level).getBlockEntity(pos);
        order.verify(level).gameEvent(eq(GameEvent.BLOCK_DESTROY), eq(pos), any(GameEvent.Context.class));
        order.verify(level).setBlock(pos, replacement, Block.UPDATE_ALL);
        verify(level, times(1)).getBlockEntity(pos);
    }
}
