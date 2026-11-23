package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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

    @GameTest(template = "infusion_room", batch = "crafttweaker_infusion_variants", timeoutTicks = 240)
    public static void overlappingComponentsUseACompleteAssignment(GameTestHelper test) {
        runVariant(test, "overlapping_items", new ItemStack(Items.CLAY_BALL),
                List.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.COPPER_INGOT)), TTAspects.TERRA, 4,
                stack -> stack.is(Items.REDSTONE));
    }

    @GameTest(template = "infusion_room", batch = "crafttweaker_infusion_variants", timeoutTicks = 240)
    public static void catalystPatchRetainsTheOriginalStack(GameTestHelper test) {
        ItemStack catalyst = new ItemStack(TTItems.FORTRESS_HELM.get());
        catalyst.setHoverName(Component.literal("Named infusion catalyst"));
        runVariant(test, "test_patch", catalyst, List.of(new ItemStack(Items.GOLD_INGOT)), TTAspects.TERRA, 4,
                stack -> stack.is(catalyst.getItem()) && stack.getHoverName().equals(catalyst.getHoverName())
                        && ItemData.getOrDefault(stack, TTDataComponents.FORTRESS_MASK.get(), 0) == 2);
    }

    @GameTest(template = "infusion_room", batch = "crafttweaker_infusion_variants", timeoutTicks = 240)
    public static void infusionEnchantmentPaysWithTagEssentia(GameTestHelper test) {
        ItemStack catalyst = new ItemStack(Items.DIAMOND_PICKAXE);
        catalyst.setDamageValue(11);
        runVariant(test, "test_enchantment", catalyst, List.of(new ItemStack(Items.APPLE)), TTAspects.AQUA, 4,
                stack -> stack.is(Items.DIAMOND_PICKAXE) && stack.getDamageValue() == 11
                        && InfusionEnchantmentHelper.level(stack, InfusionEnchantment.COLLECTOR) == 1);
    }

    @GameTest(template = "infusion_room", batch = "crafttweaker_infusion_variants", timeoutTicks = 240)
    public static void runicAugmentScalesTaggedPaymentAndComponents(GameTestHelper test) {
        ItemStack catalyst = new ItemStack(Items.DIAMOND_CHESTPLATE);
        ItemData.set(catalyst, TTDataComponents.RUNIC_CHARGE.get(), 1);
        runVariant(test, "test_runic", catalyst,
                List.of(new ItemStack(Items.LEATHER), new ItemStack(Items.IRON_INGOT)), TTAspects.AQUA, 6,
                stack -> stack.is(Items.DIAMOND_CHESTPLATE) && InfusionRunicAugmentRecipe.charge(stack) == 2);
    }

    private static void runVariant(GameTestHelper test, String id, ItemStack catalyst, List<ItemStack> components,
            ResourceKey<IAspect> aspect, int payment, Predicate<ItemStack> complete) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var context = new InfusionMatrixContext(level, test.absolutePos(MATRIX), actor.getUUID());
        var matrix = placeAltar(test);
        test.setBlock(CENTER.offset(-1, -1, -1), Blocks.AIR);
        var center = pedestal(test, CENTER, catalyst);
        var supports = new ArrayList<BlockEntityPedestal>();
        for (int slot = 0; slot < components.size(); slot++) {
            supports.add(pedestal(test, CENTER.north(2).east(slot * 2), components.get(slot)));
        }
        var source = jar(test, CENTER.west(3));
        var holder = Objects.requireNonNull(Aspects.resolve(level, aspect));
        source.setStorageContents(aspect, payment + 1);
        matrix.onRightClick(level, actor);
        var saved = matrix.saveWithoutMetadata();
        saved.putFloat("Stability", BlockEntityInfusionMatrix.STABILITY_CAP);
        matrix.load(saved);
        var inspected = InfusionCraftingTransaction.inspect(context, actor, catalyst, components);
        test.assertTrue(inspected.successful() && inspected.recipeId() != null
                        && inspected.recipeId().location().equals(new ResourceLocation("crafttweaker", id))
                        && inspected.essentia().amountOf(holder) == payment,
                "Did not match the scripted infusion and its " + payment + " essentia: " + inspected);
        test.assertTrue(InfusionCraftingTransaction.start(context, actor, inspected.recipeId()) == Failure.NONE,
                "Could not start the scripted infusion " + id);
        test.startSequence().thenWaitUntil(() -> test.assertTrue(complete.test(center.getItem()),
                "Waiting for the native infusion result of " + id)).thenExecute(() -> {
            test.assertTrue(center.getItem().getCount() == 1 && supports.stream().allMatch(p -> p.getItem().isEmpty())
                            && source.getAspects().amountOf(holder) == 1,
                    "The infusion did not spend each component and the exact essentia once");
            StageGameTests.remove(test, actor);
        }).thenSucceed();
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
