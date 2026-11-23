package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityEverfullUrn;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class SeparationAndWaterGameTests {
    private static final BlockPos CORE = new BlockPos(3, 3, 3);

    private SeparationAndWaterGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_separation_water")
    public static void centrifugeRetainsTheExactPaidProductAndRemainingTicks(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var terra = Objects.requireNonNull(Aspects.resolve(level, TTAspects.TERRA));
        var ignis = Objects.requireNonNull(Aspects.resolve(level, TTAspects.IGNIS));
        var block = TTBlocks.CENTRIFUGE.get();
        var state = block.defaultBlockState();
        test.setBlock(CORE, state);
        var machine = (BlockEntityCentrifuge) test.getBlockEntity(CORE);
        var actor = StageGameTests.player(test);
        try {
            block.setPlacedBy(level, pos, state, actor, new ItemStack(block));
            test.assertTrue(machine.insert(terra, 2, Direction.DOWN, true) == 0
                            && machine.insert(terra, 2, Direction.DOWN, false) == 0,
                    "The restricted centrifuge simulated or collected Terra");
            var stages = GameStageHelper.getPlayerData(actor);
            stages.addStage("separate_terra");
            test.assertTrue(machine.insert(terra, 2, Direction.DOWN, false) == 1,
                    "The authored conversion did not take a single Terra");
            BlockEntityCentrifuge.serverTick(level, pos, state, machine);
            stages.removeStage("separate_terra");
            machine.load(machine.saveWithFullMetadata());
            BlockEntityCentrifuge.serverTick(level, pos, state, machine);
            test.assertTrue(machine.saveWithFullMetadata().getInt("ProcessLeft") == 2
                            && machine.heldAmount(Direction.UP) == 0,
                    "A suspended paid separation lost or advanced its saved work");
            stages.addStage("separate_terra");
            BlockEntityCentrifuge.serverTick(level, pos, state, machine);
            BlockEntityCentrifuge.serverTick(level, pos, state, machine);
            stages.removeStage("separate_terra");
            test.assertTrue(machine.heldAmount(Direction.DOWN) == 0
                            && machine.extract(ignis, 2, Direction.UP, false) == 1
                            && machine.extract(ignis, 2, Direction.UP, false) == 0,
                    "The authored separation did not produce exactly one Ignis once");
        } finally {
            StageGameTests.remove(test, actor);
        }
        test.succeed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_separation_water")
    public static void urnSpendsScriptedVisAndWaterOnlyWhileAuthorized(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var target = CORE.offset(-2, -1, -2);
        var aura = Objects.requireNonNull(AuraManager.chunkAt(level, pos));
        float oldVis = aura.getVis();
        var block = TTBlocks.EVERFULL_URN.get();
        var state = block.defaultBlockState();
        test.setBlock(CORE, state);
        test.setBlock(target, Blocks.CAULDRON);
        var urn = (BlockEntityEverfullUrn) test.getBlockEntity(CORE);
        var actor = StageGameTests.player(test);
        try {
            block.setPlacedBy(level, pos, state, actor, new ItemStack(block));
            aura.setVis(2);
            BlockEntityEverfullUrn.serverTick(level, pos, state, urn);
            test.assertTrue(aura.getVis() == 2 && urn.waterAmount() == 0, "A restricted urn converted aura");
            var stages = GameStageHelper.getPlayerData(actor);
            stages.addStage("conjure_water");
            BlockEntityEverfullUrn.serverTick(level, pos, state, urn);
            test.assertTrue(aura.getVis() == 1.5F && urn.waterAmount() == 200,
                    "The urn did not convert half a vis into 200 millibuckets");
            stages.removeStage("conjure_water");
            urn.load(urn.saveWithFullMetadata());
            BlockEntityEverfullUrn.serverTick(level, pos, state, urn);
            test.assertTrue(urn.waterAmount() == 200 && test.getBlockState(target).is(Blocks.CAULDRON),
                    "The saved urn distributed water after the owner's stage was revoked");
            aura.setVis(0);
            stages.addStage("conjure_water");
            BlockEntityEverfullUrn.serverTick(level, pos, state, urn);
            var cauldron = test.getBlockState(target);
            test.assertTrue(cauldron.is(Blocks.WATER_CAULDRON) && cauldron.getValue(LayeredCauldronBlock.LEVEL) == 1,
                    "The urn did not fill the adjacent cauldron by one level");
            stages.removeStage("conjure_water");
            test.assertTrue(urn.getTank().drain(200, IFluidHandler.FluidAction.EXECUTE).getAmount() == 120
                            && urn.waterAmount() == 0,
                    "The urn did not debit exactly 80 millibuckets and retain the rest for extraction");
        } finally {
            aura.setVis(oldVis);
            StageGameTests.remove(test, actor);
        }
        test.succeed();
    }
}
