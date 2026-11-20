package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityArcaneEar;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class ArcaneEarGameTests {
    private static final BlockPos EAR = new BlockPos(2, 2, 2);

    private ArcaneEarGameTests() {}

    @GameTest(template = "essentia_room")
    public static void pulseEndsAfterTenGameTicks(GameTestHelper test) {
        var ear = placeEar(test);
        test.startSequence().thenExecute(() -> {
            ear.trigger();
            expectSignal(test, true);
        }).thenIdle(9).thenExecute(() -> expectSignal(test, true))
                .thenIdle(1).thenExecute(() -> expectSignal(test, false)).thenSucceed();
    }

    @GameTest(template = "essentia_room")
    public static void retriggeredPulseKeepsItsDeadlineAfterReload(GameTestHelper test) {
        var ear = placeEar(test);
        var level = test.getLevel();
        BlockPos pos = test.absolutePos(EAR);

        test.startSequence().thenExecute(ear::trigger).thenIdle(6).thenExecute(() -> {
            ear.trigger();
            var saved = ear.saveWithFullMetadata();
            var replacement = new BlockEntityArcaneEar(pos, level.getBlockState(pos));
            level.removeBlockEntity(pos);
            level.setBlockEntity(replacement);
            replacement.load(saved);
        }).thenIdle(4).thenExecute(() -> expectSignal(test, true))
                .thenIdle(5).thenExecute(() -> expectSignal(test, true))
                .thenIdle(1).thenExecute(() -> expectSignal(test, false)).thenSucceed();
    }

    private static BlockEntityArcaneEar placeEar(GameTestHelper test) {
        test.setBlock(EAR.below(), Blocks.STONE);
        test.setBlock(EAR, TTBlocks.ARCANE_EAR.get());
        return (BlockEntityArcaneEar) test.getBlockEntity(EAR);
    }

    private static void expectSignal(GameTestHelper test, boolean enabled) {
        BlockPos pos = test.absolutePos(EAR);
        var level = test.getLevel();
        int strength = enabled ? 15 : 0;
        test.assertTrue(level.getBlockState(pos).getValue(BlockStateProperties.ENABLED) == enabled
                        && level.getSignal(pos, Direction.UP) == strength,
                "Expected Arcane Ear output " + strength);
    }
}
