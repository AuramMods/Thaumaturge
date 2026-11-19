package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.infusion.MatrixEnvironment;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class InfusionGameTests {
    private static final BlockPos MATRIX = new BlockPos(6, 4, 6);
    private static final BlockPos CENTER = MATRIX.below(2);
    private static final BlockPos SEEDS = CENTER.west(2);
    private static final BlockPos DUST = CENTER.east(2);
    private static final BlockPos ORDO = CENTER.north(3).west();
    private static final BlockPos VITREUS = CENTER.north(3);
    private static final BlockPos VINCULUM = CENTER.north(3).east();
    private static final int LEFTOVER = 7;

    private InfusionGameTests() {}

    @GameTest(template = "infusion_room", timeoutTicks = 460)
    public static void infusionBuildsAltarAndResumesWithExactJarPayment(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        Vec3 approach = Vec3.atBottomCenterOf(test.absolutePos(CENTER.south(2)));
        player.moveTo(approach.x, approach.y, approach.z);
        assembleAltar(test, player);

        test.startSequence().thenWaitUntil(() -> {
            test.assertTrue(MatrixEnvironment.validLocation(test.getLevel(), test.absolutePos(MATRIX)),
                    "Waiting for Salis Mundus to form four arcane pillars");
            for (BlockPos corner : corners()) {
                test.assertTrue(test.getBlockState(corner.above()).isAir(), "The upper arcane stone was not removed");
            }
        }).thenExecute(() -> stageInfusion(test, player)).thenWaitUntil(() -> {
            int remaining = matrix(test).remainingEssentia().totalAmount();
            test.assertTrue(matrix(test).isCrafting() && remaining > 0 && remaining < 25,
                    "Waiting for the ticking matrix to extract the first essentia");
        }).thenExecute(() -> {
            var oldMatrix = matrix(test);
            var remaining = oldMatrix.remainingEssentia();
            var saved = oldMatrix.saveWithFullMetadata();
            test.setBlock(MATRIX, Blocks.AIR);
            test.setBlock(MATRIX, TTBlocks.INFUSION_MATRIX.get());
            var replacement = matrix(test);
            replacement.load(saved);
            test.assertTrue(oldMatrix != replacement && replacement.isActive() && replacement.isCrafting()
                            && remaining.entries().equals(replacement.remainingEssentia().entries()),
                    "Saving and replacing the matrix lost its active job or paid essentia");
        }).thenWaitUntil(() -> {
            var result = pedestal(test, CENTER).getItem();
            test.assertTrue(!matrix(test).isCrafting() && result.is(TTItems.CRYSTAL_ORDO.get())
                            && result.getCount() == 1,
                    "Waiting for the reloaded matrix to finish the Ordo crystal infusion");
        }).thenExecute(() -> {
            test.assertTrue(pedestal(test, SEEDS).getItem().isEmpty() && pedestal(test, DUST).getItem().isEmpty(),
                    "The completed infusion left an ingredient on a pedestal");
            test.assertTrue(jar(test, ORDO).amount() == LEFTOVER && jar(test, VITREUS).amount() == LEFTOVER
                            && jar(test, VINCULUM).amount() == LEFTOVER,
                    "The infusion used the wrong total essentia across save and reload");
        }).thenSucceed();
    }

    private static void assembleAltar(GameTestHelper test, FakePlayer player) {
        test.setBlock(MATRIX, TTBlocks.INFUSION_MATRIX.get());
        test.setBlock(CENTER, TTBlocks.PEDESTAL_ARCANE.get());
        for (BlockPos corner : corners()) {
            test.setBlock(corner, TTBlocks.STONE_ARCANE.get());
            test.setBlock(corner.above(), TTBlocks.STONE_ARCANE.get());
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TTItems.WAND.get()));
        wandClick(test, player);
        test.assertFalse(matrix(test).isActive(), "The wand activated an incomplete altar");

        var dust = new ItemStack(TTItems.SALIS_MUNDUS.get());
        player.setItemInHand(InteractionHand.OFF_HAND, dust);
        var use = new UseOnContext(player, InteractionHand.OFF_HAND, topOf(test.absolutePos(MATRIX)));
        test.assertFalse(dust.useOn(use).consumesAction(), "The altar used Salis Mundus before infusion research");
        test.assertTrue(dust.getCount() == 1, "The research gate consumed the Salis Mundus");
        ResearchManager.complete(player, TTIds.rl("infusion"));
        test.assertTrue(dust.useOn(use).consumesAction() && dust.isEmpty(),
                "Salis Mundus did not start the completed altar blueprint");
    }

    private static void stageInfusion(GameTestHelper test, FakePlayer player) {
        test.setBlock(SEEDS, TTBlocks.PEDESTAL_ARCANE.get());
        test.setBlock(DUST, TTBlocks.PEDESTAL_ARCANE.get());
        loadJar(test, ORDO, TTAspects.ORDO, 10);
        loadJar(test, VITREUS, TTAspects.VITREUS, 10);
        loadJar(test, VINCULUM, TTAspects.VINCULUM, 5);
        placeItem(test, player, SEEDS, new ItemStack(Items.WHEAT_SEEDS));
        placeItem(test, player, DUST, new ItemStack(TTItems.SALIS_MUNDUS.get()));

        wandClick(test, player);
        test.assertTrue(matrix(test).isActive() && !matrix(test).isCrafting(),
                "The native wand callback did not activate the finished altar");
        placeItem(test, player, CENTER, new ItemStack(TTItems.ESSENTIA_CRYSTAL.get()));
        wandClick(test, player);
        test.assertFalse(matrix(test).isCrafting(), "An untyped crystal matched the Ordo catalyst");

        var ordo = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.ORDO));
        var crystal = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get());
        ItemData.set(crystal, TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(ordo, 1));
        placeItem(test, player, CENTER, crystal);
        wandClick(test, player);
        test.assertFalse(matrix(test).isCrafting(), "Crystal farming started before its research");

        ResearchManager.complete(player, TTIds.rl("crystal_farmer"));
        wandClick(test, player);
        var remaining = matrix(test).remainingEssentia();
        test.assertTrue(matrix(test).isCrafting() && remaining.totalAmount() == 25 && remaining.amountOf(ordo) == 10,
                "The unlocked native recipe did not start with its listed essentia costs");
    }

    private static void placeItem(GameTestHelper test, FakePlayer player, BlockPos relative, ItemStack stack) {
        player.setItemInHand(InteractionHand.OFF_HAND, stack);
        var hit = topOf(test.absolutePos(relative));
        test.assertTrue(test.getBlockState(relative).use(test.getLevel(), player, InteractionHand.OFF_HAND, hit)
                        .consumesAction() && player.getOffhandItem().isEmpty(),
                "A survival interaction did not transfer the item to the pedestal at " + relative);
    }

    private static void loadJar(GameTestHelper test, BlockPos pos, ResourceKey<IAspect> key, int cost) {
        test.setBlock(pos, TTBlocks.JAR_NORMAL.get());
        var jar = jar(test, pos);
        jar.setAspectFilter(key);
        var aspect = Objects.requireNonNull(Aspects.resolve(test.getLevel(), key));
        var conduit = jar.getCapability(EssentiaCapabilities.CONDUIT, Direction.UP).orElseThrow(IllegalStateException::new);
        test.assertTrue(conduit.insert(aspect, cost + LEFTOVER, Direction.UP, false) == cost + LEFTOVER,
                "The jar did not accept its infusion stock");
    }

    private static void wandClick(GameTestHelper test, FakePlayer player) {
        ItemStack wand = player.getMainHandItem();
        var use = new UseOnContext(player, InteractionHand.MAIN_HAND, topOf(test.absolutePos(MATRIX)));
        test.assertTrue(wand.getItem().onItemUseFirst(wand, use).consumesAction(),
                "Forge's wand interaction did not reach the matrix");
    }

    private static BlockHitResult topOf(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
    }

    private static List<BlockPos> corners() {
        return List.of(CENTER.north().west(), CENTER.north().east(), CENTER.south().west(), CENTER.south().east());
    }

    private static BlockEntityInfusionMatrix matrix(GameTestHelper test) {
        return (BlockEntityInfusionMatrix) test.getBlockEntity(MATRIX);
    }

    private static BlockEntityPedestal pedestal(GameTestHelper test, BlockPos pos) {
        return (BlockEntityPedestal) test.getBlockEntity(pos);
    }

    private static BlockEntityJar jar(GameTestHelper test, BlockPos pos) {
        return (BlockEntityJar) test.getBlockEntity(pos);
    }
}
