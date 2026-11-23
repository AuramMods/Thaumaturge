package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class DistillationStageGameTests {
    private static final BlockPos CORE = new BlockPos(2, 2, 2);

    private DistillationStageGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_distillation")
    public static void smelterDistillsTheAuthoredAspectAfterOwnerGrant(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var ignis = Objects.requireNonNull(Aspects.resolve(level, TTAspects.IGNIS));
        var block = TTBlocks.SMELTER_VOID.get();
        var input = new ItemStack(Items.ECHO_SHARD);
        test.setBlock(CORE, block);
        var furnace = (BlockEntitySmelter) test.getBlockEntity(CORE);
        block.setPlacedBy(level, test.absolutePos(CORE), furnace.getBlockState(), actor, new ItemStack(block));
        test.assertTrue(furnace.itemSlots().insertItem(0, input, false).isEmpty(),
                "The smelter rejected its script-defined input");
        furnace.itemSlots().setStackInSlot(1, new ItemStack(Items.COAL));
        test.startSequence().thenIdle(12).thenExecute(() -> {
            test.assertTrue(furnace.essentiaStock.isEmpty() && furnace.fuelRemaining == 0
                            && furnace.itemSlots().getStackInSlot(1).is(Items.COAL),
                    "A furnace without the output aspect stage consumed fuel or issued essentia");
            stages.addStage("distill_ignis");
        }).thenWaitUntil(() -> test.assertTrue(furnace.essentiaStock.amountOf(ignis) == 4,
                "Waiting for exactly four Ignis from the configured input")).thenExecute(() -> {
            test.assertTrue(furnace.essentiaStock.totalAmount() == 4
                            && furnace.itemSlots().getStackInSlot(0).isEmpty()
                            && furnace.itemSlots().getStackInSlot(1).isEmpty() && furnace.fuelRemaining > 0,
                    "The distillation did not spend one item and one fuel charge");
            StageGameTests.remove(test, actor);
        }).thenSucceed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_advanced_distillation")
    public static void advancedFurnaceHoldsAStagedChargeAcrossSave(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var aqua = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AQUA));
        var block = TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get();
        EssentiaGameTests.formAdvancedFurnace(test, CORE);
        var furnace = (BlockEntityAdvancedAlchemicalFurnace) test.getBlockEntity(CORE);
        block.setPlacedBy(level, test.absolutePos(CORE), furnace.getBlockState(), actor, new ItemStack(block));
        var bush = new ItemStack(Items.DEAD_BUSH);
        test.startSequence().thenWaitUntil(() -> test.assertTrue(furnace.isAssembled(),
                "Waiting for the assembled furnace")).thenExecute(() -> {
            test.assertTrue(!furnace.insertInput(bush) && furnace.input().isEmpty(),
                    "Direct input bypassed the owner stage");
            stages.addStage("distill_advanced");
            test.assertTrue(furnace.insertInput(bush), "The scripted advanced input was rejected");
            var saved = furnace.saveWithFullMetadata();
            saved.putInt("Heat", 40);
            saved.putInt("Perditio", 20);
            saved.putInt("Aqua", 20);
            stages.removeStage("distill_advanced");
            furnace.load(saved);
        }).thenIdle(10).thenExecute(() -> {
            test.assertTrue(furnace.heat() == 40 && furnace.perditio() == 20 && furnace.aqua() == 20
                            && furnace.input().is(Items.DEAD_BUSH) && furnace.aspects().isEmpty(),
                    "A saved, stage-denied furnace spent power or the input");
            stages.addStage("distill_advanced");
        }).thenWaitUntil(() -> test.assertTrue(furnace.aspects().amountOf(aqua) == 6,
                "Waiting for the scripted Aqua output")).thenExecute(() -> {
            test.assertTrue(furnace.aspects().totalAmount() == 6 && furnace.input().isEmpty()
                            && furnace.perditio() == 14 && furnace.aqua() == 14
                            && furnace.heat() >= 27 && furnace.heat() <= 28,
                    "Advanced distillation did not use its normal 2/1/1 power costs per output");
            StageGameTests.remove(test, actor);
        }).thenSucceed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_deconstruction")
    public static void deconstructionRetainsAResultForAnEligibleCollector(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var other = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var terra = Objects.requireNonNull(Aspects.resolve(level, TTAspects.TERRA));
        var block = TTBlocks.DECONSTRUCTION_TABLE.get();
        test.setBlock(CORE, block);
        var table = (BlockEntityDeconstructionTable) test.getBlockEntity(CORE);
        block.setPlacedBy(level, test.absolutePos(CORE), table.getBlockState(), actor, new ItemStack(block));
        AspectPools.grant(actor, terra, 1);
        int initial = AspectPools.amount(actor, terra);
        test.assertTrue(table.items().insertItem(0, new ItemStack(Items.ECHO_SHARD), false).isEmpty(),
                "The table rejected the authored input");
        test.startSequence().thenIdle(42).thenExecute(() -> {
            test.assertTrue(table.resultAspect() == null && table.items().getStackInSlot(0).is(Items.ECHO_SHARD),
                    "The table consumed an item before its owner met the stage");
            stages.addStage("deconstruct_echo");
        }).thenWaitUntil(() -> test.assertTrue(TTAspects.TERRA.location().equals(table.resultAspect()),
                "Waiting for the scripted research aspect")).thenExecute(() -> {
            table.load(table.saveWithFullMetadata());
            table.collect(other);
            test.assertTrue(TTAspects.TERRA.location().equals(table.resultAspect()),
                    "The saved result lost the input's stage or was cleared by a denied collector");
            table.collect(actor);
            table.collect(actor);
            test.assertTrue(table.resultAspect() == null && table.items().getStackInSlot(0).isEmpty()
                            && AspectPools.amount(actor, terra) == initial + 3,
                    "The scripted result did not grant three points once");
            StageGameTests.remove(test, other);
            StageGameTests.remove(test, actor);
        }).thenSucceed();
    }
}
