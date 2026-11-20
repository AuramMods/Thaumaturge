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
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
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

    @GameTest(template = "infusion_room", batch = "cloudstepper_ring", timeoutTicks = 640)
    public static void cloudstepperRingReportsMissingIngredientsAndSpendsItsInputs(GameTestHelper test) {
        var feedback = new AtomicReference<String>();
        var player = new FakePlayer(test.getLevel(), new GameProfile(UUID.randomUUID(), "InfusionTest")) {
            @Override
            public void displayClientMessage(Component message, boolean overlay) {
                if (overlay && message.getContents() instanceof TranslatableContents translated) {
                    feedback.set(translated.getKey());
                }
            }
        };
        player.setGameMode(GameType.SURVIVAL);
        var approach = Vec3.atBottomCenterOf(test.absolutePos(CENTER.south(2)));
        player.moveTo(approach.x, approach.y, approach.z);
        for (int distance = 2; distance <= 4; distance++) {
            test.setBlock(CENTER.offset(distance, 0, -2), Blocks.SKELETON_SKULL);
            test.setBlock(CENTER.offset(-distance, 0, 2), Blocks.SKELETON_SKULL);
        }
        assembleAltar(test, player);
        ResearchManager.complete(player, TTIds.rl("cloud_ring"));

        test.startSequence().thenWaitUntil(() -> test.assertTrue(
                MatrixEnvironment.validLocation(test.getLevel(), test.absolutePos(MATRIX)), "Waiting for the finished altar"))
                .thenExecute(() -> {
                    test.assertTrue(MatrixEnvironment.survey(test.getLevel(), test.absolutePos(MATRIX))
                                    .stabilityReplenish() > 0.2F,
                            "The three matched skull pairs should offset the ring recipe's stability loss");
                    test.setBlock(SEEDS, TTBlocks.PEDESTAL_ARCANE.get());
                    test.setBlock(DUST, TTBlocks.PEDESTAL_ARCANE.get());
                    loadJar(test, ORDO, TTAspects.AER, 50);
                    placeItem(test, player, CENTER, new ItemStack(TTItems.RING_MUNDANE.get()));
                    var aer = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.AER));
                    var crystal = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get());
                    ItemData.set(crystal, TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aer, 1));
                    placeItem(test, player, SEEDS, crystal);
                    wandClick(test, player);
                    wandClick(test, player);
                    test.assertTrue(!matrix(test).isCrafting() && jar(test, ORDO).amount() == 50 + LEFTOVER
                                    && "message.thaumaturge.infusion.no_recipe".equals(feedback.get()),
                            "Missing the feather should explain the rejected recipe and spend nothing");

                    placeItem(test, player, DUST, new ItemStack(Items.FEATHER));
                    player.setGameMode(GameType.CREATIVE);
                    wandClick(test, player);
                    test.assertTrue(matrix(test).isCrafting() && matrix(test).remainingEssentia().amountOf(aer) == 50,
                            "Adding a vanilla feather should start the Cloudstepper Ring infusion");
                }).thenWaitUntil(() -> test.assertTrue(
                        !matrix(test).isCrafting() && pedestal(test, CENTER).getItem().is(TTItems.CLOUD_RING.get()),
                        "Waiting for the Cloudstepper Ring")).thenExecute(() -> {
                    test.assertTrue(pedestal(test, SEEDS).getItem().isEmpty() && pedestal(test, DUST).getItem().isEmpty()
                                    && jar(test, ORDO).amount() == LEFTOVER,
                            "The creative caster should still use both altar ingredients and 50 Aer from a finite jar");
                }).thenSucceed();
    }

    @GameTest(template = "infusion_room", batch = "infusion_recovery", timeoutTicks = 650)
    public static void missingInfusionIngredientResumesAfterReloadWithOneExtraAer(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        var approach = Vec3.atBottomCenterOf(test.absolutePos(CENTER.south(2)));
        player.moveTo(approach.x, approach.y, approach.z);
        assembleAltar(test, player);
        ResearchManager.complete(player, TTIds.rl("cloud_ring"));

        test.startSequence().thenWaitUntil(() -> test.assertTrue(
                MatrixEnvironment.validLocation(level, test.absolutePos(MATRIX)), "Waiting for the finished altar"))
                .thenExecute(() -> startFundedCloudstepper(test, player))
                .thenWaitUntil(() -> test.assertTrue(matrix(test).isCrafting() && pedestal(test, SEEDS).getItem().isEmpty(),
                        "Waiting for the crystal to be consumed")).thenExecute(() -> {
                    test.assertTrue(jar(test, ORDO).amount() == 0 && matrix(test).remainingEssentia().isEmpty(),
                            "The original 50 Aer should already be paid");
                    var pos = test.absolutePos(DUST);
                    test.assertTrue(level.getBlockState(pos).use(level, player, InteractionHand.OFF_HAND, topOf(pos))
                                    .consumesAction() && pedestal(test, DUST).getItem().isEmpty()
                                    && player.getInventory().countItem(Items.FEATHER) == 1,
                            "Picking up the outstanding feather should transfer exactly one item");
                }).thenWaitUntil(() -> test.assertTrue(matrix(test).remainingEssentia().totalAmount() == 1,
                        "Waiting for a single missing-ingredient surcharge")).thenExecute(() -> {
                    var saved = matrix(test).saveWithFullMetadata();
                    test.setBlock(MATRIX, Blocks.AIR);
                    test.setBlock(MATRIX, TTBlocks.INFUSION_MATRIX.get());
                    matrix(test).load(saved);
                }).thenIdle(25).thenExecute(() -> {
                    var aer = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AER));
                    test.assertTrue(matrix(test).isCrafting() && matrix(test).remainingEssentia().amountOf(aer) == 1
                                    && pedestal(test, CENTER).getItem().is(TTItems.RING_MUNDANE.get()),
                            "The restored job should stall on its one Aer debt without producing a ring");
                    var inventory = player.getInventory();
                    int slot = inventory.findSlotMatchingItem(new ItemStack(Items.FEATHER));
                    test.assertTrue(slot >= 0, "The player should still have the original feather");
                    placeItem(test, player, DUST, inventory.removeItem(slot, 1));
                    jar(test, ORDO).setStorageContents(TTAspects.AER, 1);
                }).thenWaitUntil(() -> test.assertTrue(!matrix(test).isCrafting()
                                && pedestal(test, CENTER).getItem().is(TTItems.CLOUD_RING.get()),
                        "Waiting for the restored infusion to finish")).thenExecute(() -> {
                    test.assertTrue(pedestal(test, SEEDS).getItem().isEmpty() && pedestal(test, DUST).getItem().isEmpty()
                                    && jar(test, ORDO).amount() == 0 && player.getInventory().countItem(Items.FEATHER) == 0,
                            "The same job should complete after one additional Aer and consume the returned feather once");
                }).thenSucceed();
    }

    @GameTest(template = "infusion_room", batch = "infusion_abort", timeoutTicks = 820)
    public static void interruptedPullDoesNotShortenTheNextInfusion(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        var approach = Vec3.atBottomCenterOf(test.absolutePos(CENTER.south(2)));
        player.moveTo(approach.x, approach.y, approach.z);
        assembleAltar(test, player);
        ResearchManager.complete(player, TTIds.rl("cloud_ring"));

        BlockPos pillar = corners().get(0);
        test.startSequence().thenWaitUntil(() -> test.assertTrue(
                MatrixEnvironment.validLocation(level, test.absolutePos(MATRIX)), "Waiting for the finished altar"))
                .thenExecute(() -> startFundedCloudstepper(test, player))
                .thenWaitUntil(() -> test.assertTrue(matrix(test).isCrafting() && matrix(test).remainingEssentia().isEmpty(),
                        "Waiting for the first job to pay its Aer"))
                .thenIdle(10).thenExecute(() -> test.setBlock(pillar, Blocks.AIR))
                .thenWaitUntil(() -> test.assertTrue(!matrix(test).isActive() && !matrix(test).isCrafting(),
                        "The missing pillar should abort the first infusion")).thenExecute(() -> {
                    test.assertTrue(pedestal(test, SEEDS).getItem().is(TTItems.ESSENTIA_CRYSTAL.get())
                                    && pedestal(test, DUST).getItem().is(Items.FEATHER) && jar(test, ORDO).amount() == 0,
                            "The aborted job should retain both ingredients and leave the already spent Aer spent");
                    test.setBlock(pillar, TTBlocks.PILLAR_ARCANE.get());
                    jar(test, ORDO).setStorageContents(TTAspects.AER, 50);
                    wandClick(test, player);
                    wandClick(test, player);
                }).thenWaitUntil(() -> test.assertTrue(matrix(test).isCrafting() && matrix(test).remainingEssentia().isEmpty(),
                        "Waiting for the second job to pay its Aer"))
                .thenIdle(20).thenExecute(() -> test.assertTrue(
                        pedestal(test, SEEDS).getItem().is(TTItems.ESSENTIA_CRYSTAL.get()),
                        "A new infusion should begin a full ingredient pull after the previous job is aborted"))
                .thenWaitUntil(() -> test.assertTrue(!matrix(test).isCrafting()
                                && pedestal(test, CENTER).getItem().is(TTItems.CLOUD_RING.get()),
                        "Waiting for the second job to produce its result"))
                .thenExecute(() -> test.assertTrue(pedestal(test, SEEDS).getItem().isEmpty()
                                && pedestal(test, DUST).getItem().isEmpty() && jar(test, ORDO).amount() == 0,
                        "The second job should consume both ingredients and its own 50 Aer")).thenSucceed();
    }

    private static void startFundedCloudstepper(GameTestHelper test, FakePlayer player) {
        test.setBlock(SEEDS, TTBlocks.PEDESTAL_ARCANE.get());
        test.setBlock(DUST, TTBlocks.PEDESTAL_ARCANE.get());
        test.setBlock(ORDO, TTBlocks.JAR_NORMAL.get());
        jar(test, ORDO).setStorageContents(TTAspects.AER, 50);
        placeItem(test, player, CENTER, new ItemStack(TTItems.RING_MUNDANE.get()));
        var crystal = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get());
        var aer = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.AER));
        ItemData.set(crystal, TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aer, 1));
        placeItem(test, player, SEEDS, crystal);
        placeItem(test, player, DUST, new ItemStack(Items.FEATHER));
        wandClick(test, player);
        var ready = matrix(test).saveWithoutMetadata();
        ready.putFloat("Stability", BlockEntityInfusionMatrix.STABILITY_CAP);
        matrix(test).load(ready);
        wandClick(test, player);
    }

    @GameTest(template = "infusion_room")
    public static void creativePedestalPickupClearsRemoteDisplay(GameTestHelper test) {
        var level = test.getLevel();
        var player = ResearchGameTests.survivalPlayer(test);
        player.setGameMode(GameType.CREATIVE);
        test.setBlock(CENTER, TTBlocks.PEDESTAL_ARCANE.get());
        var pos = test.absolutePos(CENTER);
        var state = level.getBlockState(pos);
        var hit = topOf(pos);
        var actual = pedestal(test, CENTER);
        var display = new BlockEntityPedestal(pos, state);
        display.setLevel(level);
        var ingredients = new ItemStack(Items.FEATHER, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, ingredients);
        test.assertTrue(state.use(level, player, InteractionHand.MAIN_HAND, hit).consumesAction()
                        && ingredients.getCount() == 3, "Creative placement should keep the player's stack");
        deliverPedestalUpdate(actual, display);
        test.assertTrue(display.getItem().is(Items.FEATHER) && display.getItem().getCount() == 1,
                "The displayed pedestal should receive the staged feather");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        test.assertTrue(state.use(level, player, InteractionHand.MAIN_HAND, hit).consumesAction()
                        && player.getInventory().countItem(Items.FEATHER) == 1 && actual.getItem().isEmpty(),
                "An empty-hand click should take exactly one item and empty the server pedestal");
        deliverPedestalUpdate(actual, display);
        test.assertTrue(display.getItem().isEmpty(), "The native update packet should clear the displayed feather");
        test.succeed();
    }

    private static void deliverPedestalUpdate(BlockEntityPedestal actual, BlockEntityPedestal display) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var packet = (ClientboundBlockEntityDataPacket) actual.getUpdatePacket();
            packet.write(buffer);
            display.onDataPacket(null, new ClientboundBlockEntityDataPacket(buffer));
        } finally {
            buffer.release();
        }
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
