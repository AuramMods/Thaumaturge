package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityCondenser;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVisGenerator;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockEntityFluxScrubber;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class AuraMachineGameTests {
    private static final String STAGE = "operate_aura_machines";
    private static final BlockPos CORE = new BlockPos(2, 2, 2);

    private AuraMachineGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_aura_machines")
    public static void condenserChecksOwnerBeforeIntakeAndConversion(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var actor = StageGameTests.player(test);
        var aura = Objects.requireNonNull(AuraManager.chunkAt(level, pos));
        var aqua = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AQUA));
        var vitium = Objects.requireNonNull(Aspects.resolve(level, TTAspects.VITIUM));
        float oldFlux = aura.getFlux();
        try {
            aura.setFlux(8);
            var block = TTBlocks.CONDENSER.get();
            var state = block.defaultBlockState();
            test.setBlock(CORE, state);
            test.setBlock(CORE.above(), TTBlocks.CONDENSER_LATTICE.get());
            var condenser = (BlockEntityCondenser) test.getBlockEntity(CORE);
            block.setPlacedBy(level, pos, state, actor, new ItemStack(block));
            test.assertTrue(condenser.insert(aqua, 3, Direction.NORTH, true) == 0
                            && condenser.insert(aqua, 3, Direction.NORTH, false) == 0,
                    "A restricted condenser advertised or accepted essentia");
            var stages = GameStageHelper.getPlayerData(actor);
            stages.addStage(STAGE);
            test.assertTrue(condenser.insert(aqua, 3, Direction.NORTH, false) == 3,
                    "The owner could not load the condenser");
            stages.removeStage(STAGE);
            condenser.load(condenser.saveWithFullMetadata());
            BlockEntityCondenser.serverTick(level, pos, state, condenser);
            test.assertTrue(aura.getFlux() == 8 && condenser.heldAmount(Direction.DOWN) == 0,
                    "A saved, restricted condenser paid aura or emitted output");
            stages.addStage(STAGE);
            BlockEntityCondenser.serverTick(level, pos, state, condenser);
            test.assertTrue(aura.getFlux() == 5 && condenser.saveWithFullMetadata().getInt("essentia") == 1,
                    "The condenser did not spend exactly three flux and two essentia");
            stages.removeStage(STAGE);
            test.assertTrue(condenser.extract(vitium, 10, Direction.DOWN, false) == 2
                            && condenser.extract(vitium, 10, Direction.DOWN, false) == 0,
                    "The completed two Vitium was unavailable or could be collected twice");
        } finally {
            aura.setFlux(oldFlux);
            StageGameTests.remove(test, actor);
        }
        test.succeed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_aura_machines")
    public static void visGeneratorOnlyPaysForAnEligibleOwner(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var actor = StageGameTests.player(test);
        var aura = Objects.requireNonNull(AuraManager.chunkAt(level, pos));
        float oldVis = aura.getVis();
        try {
            aura.setVis(8);
            var block = TTBlocks.VIS_GENERATOR.get();
            var state = block.defaultBlockState();
            test.setBlock(CORE.above(), Blocks.AIR);
            test.setBlock(CORE, state);
            var generator = (BlockEntityVisGenerator) test.getBlockEntity(CORE);
            block.setPlacedBy(level, pos, state, actor, new ItemStack(block));
            BlockEntityVisGenerator.serverTick(level, pos, state, generator);
            test.assertTrue(aura.getVis() == 8 && generator.getEnergyStored() == 0,
                    "A restricted generator drew aura or generated energy");
            var stages = GameStageHelper.getPlayerData(actor);
            stages.addStage(STAGE);
            BlockEntityVisGenerator.serverTick(level, pos, state, generator);
            test.assertTrue(aura.getVis() == 6 && generator.getEnergyStored() == 600,
                    "The eligible owner did not convert two vis to 600 FE");
            stages.removeStage(STAGE);
            generator.load(generator.saveWithFullMetadata());
            BlockEntityVisGenerator.serverTick(level, pos, state, generator);
            test.assertTrue(aura.getVis() == 6 && generator.getEnergyStored() == 600,
                    "The saved paid energy changed on revocation");
        } finally {
            aura.setVis(oldVis);
            StageGameTests.remove(test, actor);
        }
        test.succeed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_aura_machines")
    public static void scrubberSpendsScriptedAerAndRecoversEssentia(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var fluxPos = test.absolutePos(CORE.above());
        var block = TTBlocks.FLUX_SCRUBBER.get();
        var state = block.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN);
        test.setBlock(CORE, state);
        test.setBlock(CORE.above(), TTBlocks.FLUX_GAS.get().withFluxAmount(3));
        var scrubber = (BlockEntityFluxScrubber) test.getBlockEntity(CORE);
        var actor = StageGameTests.player(test);
        try {
            block.setPlacedBy(level, pos, state, actor, new ItemStack(block));
            var stored = scrubber.saveWithFullMetadata();
            stored.putInt("Power", 5);
            scrubber.load(stored);
            BlockEntityFluxScrubber.serverTick(level, pos, state, scrubber);
            test.assertTrue(scrubber.power() == 5 && PhysicalFlux.amount(level, fluxPos) == 3,
                    "A restricted scrubber spent Aer or changed physical flux");
            var stages = GameStageHelper.getPlayerData(actor);
            stages.addStage(STAGE);
            // A scan visits each cell in the 33-by-33-by-33 cube at 16 cells per tick.
            for (int tick = 0; scrubber.power() == 5 && tick < 2247; tick++) {
                BlockEntityFluxScrubber.serverTick(level, pos, state, scrubber);
            }
            test.assertTrue(scrubber.power() == 2 && PhysicalFlux.amount(level, fluxPos) == 1
                            && scrubber.charges() == 0,
                    "The scrubber did not remove two flux units for three Aer and spend the resulting charges");
            stages.removeStage(STAGE);
            var magic = Objects.requireNonNull(Aspects.resolve(level, TTAspects.PRAECANTATIO));
            test.assertTrue(scrubber.extract(magic, 3, Direction.DOWN, false) == 2
                            && scrubber.extract(magic, 3, Direction.DOWN, false) == 0,
                    "The paid recovery did not yield exactly two Praecantatio once");
        } finally {
            test.setBlock(CORE.above(), Blocks.AIR);
            StageGameTests.remove(test, actor);
        }
        test.succeed();
    }
}
