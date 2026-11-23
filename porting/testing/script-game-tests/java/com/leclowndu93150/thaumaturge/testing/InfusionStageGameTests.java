package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class InfusionStageGameTests {
    private static final BlockPos MATRIX = new BlockPos(6, 4, 6);
    private static final BlockPos CENTER = MATRIX.below(2);

    private InfusionStageGameTests() {}

    @GameTest(template = "infusion_room", batch = "crafttweaker_tagged_infusion", timeoutTicks = 240)
    public static void taggedInfusionRequiresFullPaymentAndResumesItsConcretePlan(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var aqua = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AQUA));
        var terra = Objects.requireNonNull(Aspects.resolve(level, TTAspects.TERRA));
        var context = new InfusionMatrixContext(level, test.absolutePos(MATRIX), actor.getUUID());
        var catalyst = new ItemStack(Items.PACKED_MUD);
        var component = new ItemStack(Items.NETHER_WART);
        var matrix = placeAltar(test);
        var center = pedestal(test, CENTER, catalyst);
        var side = pedestal(test, CENTER.north(2), component);
        var aquaJar = jar(test, CENTER.east(3));
        var terraJar = jar(test, CENTER.west(3));
        aquaJar.setStorageContents(TTAspects.AQUA, 3);
        terraJar.setStorageContents(TTAspects.TERRA, 8);
        matrix.onRightClick(level, actor);
        var ready = matrix.saveWithoutMetadata();
        ready.putFloat("Stability", BlockEntityInfusionMatrix.STABILITY_CAP);
        matrix.load(ready);

        var inspection = InfusionCraftingTransaction.inspect(context, actor, catalyst, List.of(component));
        test.assertTrue(inspection.failure() == Failure.STAGE_LOCKED, "The configured stage did not lock the recipe");
        test.assertTrue(inspection.recipeId() != null
                        && inspection.recipeId().location().equals(new ResourceLocation("crafttweaker", "tagged_matrix")),
                "The matrix did not select the scripted recipe");
        stages.addStage("tagged_infusion");
        test.assertTrue(InfusionCraftingTransaction.start(context, actor, inspection.recipeId())
                        == Failure.ESSENTIA_UNAVAILABLE && !matrix.isCrafting(),
                "The matrix started with an incomplete shared payment");
        test.assertTrue(aquaJar.getAspects().amountOf(aqua) == 3 && terraJar.getAspects().amountOf(terra) == 8
                        && center.getItem().is(Items.PACKED_MUD) && side.getItem().is(Items.NETHER_WART),
                "A failed inspection spent ingredients");

        terraJar.setStorageContents(TTAspects.TERRA, 10);
        var eligible = InfusionCraftingTransaction.inspect(context, actor, catalyst, List.of(component));
        test.assertTrue(eligible.successful() && eligible.essentia().amountOf(aqua) == 3
                        && eligible.essentia().amountOf(terra) == 9,
                "The scripted upgrade and overlapping tags did not yield the concrete scaled payment");
        test.assertTrue(InfusionCraftingTransaction.start(context, actor, eligible.recipeId()) == Failure.NONE,
                "A funded, eligible player could not start the infusion");
        stages.removeStage("tagged_infusion");
        matrix.load(matrix.saveWithFullMetadata());
        test.startSequence().thenIdle(15).thenExecute(() -> {
            test.assertTrue(matrix.remainingEssentia().totalAmount() == 12 && side.getItem().is(Items.NETHER_WART),
                    "Revocation failed to hold the saved job");
            test.assertTrue(aquaJar.getAspects().amountOf(aqua) == 3 && terraJar.getAspects().amountOf(terra) == 10,
                    "A paused job charged essentia");
            stages.addStage("tagged_infusion");
        }).thenWaitUntil(() -> test.assertTrue(center.getItem().is(Items.DIAMOND),
                "Waiting for the resumed infusion output")).thenExecute(() -> {
            test.assertTrue(center.getItem().getCount() == 1 && side.getItem().isEmpty()
                            && aquaJar.getAspects().isEmpty() && terraJar.getAspects().amountOf(terra) == 1,
                    "The resumed infusion did not consume its exact payment once");
            StageGameTests.remove(test, actor);
        }).thenSucceed();
    }

    private static BlockEntityInfusionMatrix placeAltar(GameTestHelper test) {
        test.setBlock(MATRIX, TTBlocks.INFUSION_MATRIX.get());
        for (int x : new int[] {-1, 1}) {
            for (int z : new int[] {-1, 1}) {
                test.setBlock(CENTER.offset(x, 0, z), TTBlocks.PILLAR_ARCANE.get());
            }
        }
        test.setBlock(CENTER.offset(-1, -1, -1), Blocks.BLUE_WOOL);
        return (BlockEntityInfusionMatrix) test.getBlockEntity(MATRIX);
    }

    private static BlockEntityPedestal pedestal(GameTestHelper test, BlockPos pos, ItemStack stack) {
        test.setBlock(pos, TTBlocks.PEDESTAL_ARCANE.get());
        var pedestal = (BlockEntityPedestal) test.getBlockEntity(pos);
        pedestal.setItem(stack.copy());
        return pedestal;
    }

    private static BlockEntityJar jar(GameTestHelper test, BlockPos pos) {
        test.setBlock(pos, TTBlocks.JAR_NORMAL.get());
        return (BlockEntityJar) test.getBlockEntity(pos);
    }
}
