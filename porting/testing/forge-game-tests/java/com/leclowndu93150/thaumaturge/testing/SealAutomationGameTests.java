package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.SupplyOrder;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.EmptyBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.HarvestBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.ProvideBehavior;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class SealAutomationGameTests {
    private static final BlockPos SOURCE = new BlockPos(2, 2, 5);
    private static final BlockPos TARGET = new BlockPos(6, 2, 5);
    private static final BlockPos SPAWN = new BlockPos(4, 2, 2);
    private static final BlockPos HARVEST_SEAL = new BlockPos(4, 2, 7);
    private static final BlockPos HARVEST_CROP = HARVEST_SEAL.north();
    private static final Direction FACE = Direction.NORTH;

    private SealAutomationGameTests() {}

    @GameTest(template = "golem_room", batch = "stock_seal", timeoutTicks = 900)
    public static void stockSealRefillsThroughSingleItemProvider(GameTestHelper test) {
        var owner = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        buildWorkshop(test);
        var source = chest(test, SOURCE);
        var target = chest(test, TARGET);
        var provider = placeSeal(test, owner, SOURCE, new ItemStack(TTItems.SEAL_PROVIDER.get()));
        configureFilter(provider);
        provider.setSetting(ProvideBehavior.SINGLE_ITEM, true);
        provider.setSetting(ProvideBehavior.LEAVE_ONE, true);
        provider.markChanged(level);
        var stock = placeSeal(test, owner, TARGET, new ItemStack(TTItems.SEAL_STOCK.get()));
        configureFilter(stock).setLimit(0, 5);
        stock.markChanged(level);

        var worker = createWorker(test, owner);
        test.assertTrue(level.addFreshEntity(worker), "Could not add the stock worker");
        test.startSequence().thenWaitUntil(() -> {
            test.assertTrue(target.countItem(Items.IRON_INGOT) == 5,
                    "Waiting for five stocked ingots. Stocked " + target.countItem(Items.IRON_INGOT)
                            + ", source " + source.countItem(Items.IRON_INGOT) + ", held " + worker.getMainHandItem()
                            + ", rank XP " + worker.getRankXp());
        }).thenExecute(() -> {
            test.assertTrue(worker.getRankXp() == 5 && worker.getMainHandItem().isEmpty(),
                    "Five single-item trips should leave the golem empty-handed with five rank XP");
            assertPreservedSource(test);
            test.assertTrue(target.removeItem(0, 2).getCount() == 2, "Could not take two ingots from stock");
        }).thenIdle(110).thenExecute(() -> {
            test.assertTrue(target.countItem(Items.IRON_INGOT) == 3 && worker.getRankXp() == 5,
                    "The provider should preserve its reserve while stock is low");
            assertPreservedSource(test);
            source.setItem(0, new ItemStack(Items.IRON_INGOT, 3));
        }).thenWaitUntil(() -> {
            test.assertTrue(target.countItem(Items.IRON_INGOT) == 5,
                    "Waiting for the stock seal to refill the same container after replenishment");
        }).thenIdle(110).thenExecute(() -> {
            test.assertTrue(target.countItem(Items.IRON_INGOT) == 5 && worker.getRankXp() == 7
                            && worker.getMainHandItem().isEmpty(),
                    "Stock should have exactly five ingots after seven single-item trips");
            assertPreservedSource(test);
            SealHandler.removeSealEntity(level, provider.pos(), true);
            SealHandler.removeSealEntity(level, stock.pos(), true);
            worker.discard();
        }).thenSucceed();
    }

    @GameTest(template = "golem_room", batch = "harvest_supply", timeoutTicks = 600)
    public static void harvestSealRequestsOneSeedFromItsProvider(GameTestHelper test) {
        var owner = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        var harvest = buildHarvestPlot(test, owner);
        SealHandler.removeSealEntity(level, new SealPos(test.absolutePos(SOURCE), FACE), true);
        test.setBlock(SOURCE, Blocks.CHEST);
        var chest = chest(test, SOURCE);
        chest.clearContent();
        chest.setItem(0, new ItemStack(Items.NETHER_WART));
        chest.setItem(1, new ItemStack(Items.CARROT, 4));
        var provider = placeSeal(test, owner, SOURCE, new ItemStack(TTItems.SEAL_PROVIDER.get()));
        provider.setLocked(true);
        provider.setSetting(ProvideBehavior.LEAVE_ONE, true);
        var filter = provider.filter().orElseThrow();
        filter.setBlacklist(false);
        filter.setStack(0, new ItemStack(Items.NETHER_WART));
        provider.markChanged(level);

        var worker = createWorker(test, owner);
        worker.applyBuild(worker.properties().withArms(TTGolemParts.ARMS_FINE.get()));
        test.assertTrue(SealAccess.allows(harvest, worker) && SealAccess.allows(provider, worker),
                "The worker should meet both seals' requirements");
        test.assertTrue(level.addFreshEntity(worker), "Could not add the seed-provisioning worker");
        var firstRequest = new AtomicReference<SupplyOrder>();

        test.startSequence().thenWaitUntil(() -> {
            test.assertTrue(test.getBlockState(HARVEST_CROP).isAir() && worker.getRankXp() == 2,
                    "Waiting for the worker to cut the mature crop");
        }).thenIdle(30).thenExecute(() -> {
            test.assertTrue(seedRequests(test, harvest).isEmpty() && worker.getMainHandItem().isEmpty(),
                    "The Harvest seal requested supplies before the setting was enabled");
            harvest.setSetting(HarvestBehavior.REQUEST_SEEDS, true);
            harvest.markChanged(level);
        }).thenWaitUntil(() -> {
            var orders = seedRequests(test, harvest);
            test.assertTrue(orders.size() == 1, "Waiting for one automatic seed request");
            firstRequest.set(orders.get(0));
        }).thenIdle(30).thenExecute(() -> {
            test.assertTrue(seedRequests(test, harvest).equals(List.of(firstRequest.get()))
                            && chest.countItem(Items.NETHER_WART) == 1
                            && worker.getMainHandItem().isEmpty() && test.getBlockState(HARVEST_CROP).isAir(),
                    "The request should stay unique and the provider should retain its last seed");
            chest.setItem(0, new ItemStack(Items.NETHER_WART, 3));
            var order = firstRequest.get();
            for (int ticks = 0; ticks < 20 && order.servingTask() == null; ticks++) {
                provider.tick(level);
            }
            var pickup = Objects.requireNonNull(order.servingTask(), "The provider did not publish a pickup");
            var approach = Vec3.atBottomCenterOf(test.absolutePos(SOURCE.north()));
            worker.moveTo(approach.x, approach.y, approach.z);
            test.assertTrue(pickup.canBePerformedBy(worker), "The worker should be able to take the seed");
            // A new crop can request supplies before the provider next cleans its queue.
            TaskBoard.attempt(level, pickup, worker);
            TaskBoard.of(level).sweep(level);
            test.assertTrue(chest.countItem(Items.NETHER_WART) == 2 && worker.getRankXp() == 3,
                    "The pickup should remove one seed and award one rank XP");
            test.assertTrue(order.isSpent(), "A fully collected seed order should be spent");
            var next = GolemDispatch.orderSupply(level, harvest, new ItemStack(Items.NETHER_WART)).orElseThrow();
            test.assertTrue(next != order && !next.isSpent(), "A completed seed order blocked a new identical request");
            next.markSpent();
        }).thenWaitUntil(() -> {
            test.assertTrue(test.getBlockState(HARVEST_CROP).is(Blocks.NETHER_WART),
                    "Waiting for the worker to fetch and plant a seed from the chest");
        }).thenIdle(120).thenExecute(() -> {
            test.assertTrue(chest.countItem(Items.NETHER_WART) == 2 && chest.countItem(Items.CARROT) == 4
                            && worker.getMainHandItem().isEmpty() && worker.getRankXp() == 4
                            && firstRequest.get().isSpent() && seedRequests(test, harvest).isEmpty(),
                    "Expected one completed delivery and one planting. Chest stock " + chest.countItem(Items.NETHER_WART)
                            + ", hand " + worker.getMainHandItem() + ", rank XP " + worker.getRankXp()
                            + ", spent " + firstRequest.get().isSpent()
                            + ", pending requests " + seedRequests(test, harvest).size());
            SealHandler.removeSealEntity(level, harvest.pos(), true);
            SealHandler.removeSealEntity(level, provider.pos(), true);
            worker.discard();
        }).thenSucceed();
    }

    @GameTest(template = "golem_room", batch = "harvest_seal", timeoutTicks = 460)
    public static void harvestSealReplantsWithOneSeedAfterCapabilityReload(GameTestHelper test) {
        var owner = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        var cropPos = test.absolutePos(HARVEST_CROP);
        var harvest = buildHarvestPlot(test, owner);

        var worker = createWorker(test, owner);
        worker.applyBuild(worker.properties().withArms(TTGolemParts.ARMS_FINE.get()));
        test.assertTrue(SealAccess.allows(harvest, worker), "The worker should meet the Harvest seal requirements");
        test.assertTrue(level.addFreshEntity(worker), "Could not add the harvest worker");
        test.startSequence().thenWaitUntil(() -> {
            var crop = test.getBlockState(HARVEST_CROP);
            var task = worker.getTask();
            test.assertTrue(crop.isAir() && worker.getRankXp() == 2,
                    "Waiting for nether wart harvest with crop " + crop + ", worker " + worker.blockPosition()
                            + ", task " + (task == null ? "none" : task.pos()) + ", rank XP " + worker.getRankXp());
        }).thenExecute(() -> {
            int produce = level.getEntitiesOfClass(ItemEntity.class, new AABB(cropPos).inflate(2)).stream()
                    .filter(drop -> drop.getItem().is(Items.NETHER_WART))
                    .mapToInt(drop -> drop.getItem().getCount()).sum();
            test.assertTrue(produce >= 2 && produce <= 4 && worker.getMainHandItem().isEmpty(),
                    "Harvest should drop mature nether wart and wait for planting stock");

            var chunk = level.getChunkAt(harvest.pos().pos());
            var saved = chunk.writeCapsToNBT();
            SealHandler.unloadChunkSeals(level, chunk);
            chunk.readCapsFromNBT(saved);
            SealHandler.loadChunkSeals(level, chunk);
            var restored = SealHandler.getSealEntity(level, harvest.pos());
            test.assertTrue(restored != null && restored != harvest && restored.setting(HarvestBehavior.REPLANT),
                    "The chunk capability should restore the Harvest seal and its replant setting");

            test.assertTrue(worker.hands().hold(new ItemStack(Items.NETHER_WART, 2)).isEmpty(),
                    "The golem should accept the two supplied wart items");
        }).thenWaitUntil(() -> {
            var crop = test.getBlockState(HARVEST_CROP);
            var task = worker.getTask();
            var queued = TaskBoard.of(level).openBlockTasks(worker.getUUID(), worker).stream()
                    .filter(candidate -> harvest.pos().equals(candidate.origin())).count();
            test.assertTrue(crop.is(Blocks.NETHER_WART) && crop.getValue(NetherWartBlock.AGE) < NetherWartBlock.MAX_AGE,
                    "Waiting to replant with crop " + crop + ", soil " + test.getBlockState(HARVEST_CROP.below())
                            + ", worker " + worker.blockPosition() + ", task " + (task == null ? "none" : task.pos())
                            + ", " + queued + " queued tasks and " + worker.getMainHandItem());
        }).thenExecute(() -> {
            test.assertTrue(worker.getMainHandItem().is(Items.NETHER_WART)
                            && worker.getMainHandItem().getCount() == 1 && worker.getRankXp() == 3,
                    "Expected one seed and three total rank XP after replanting, found "
                            + worker.getMainHandItem() + " and " + worker.getRankXp() + " XP");
            SealHandler.removeSealEntity(level, harvest.pos(), true);
            worker.discard();
        }).thenSucceed();
    }

    @GameTest(template = "golem_room", timeoutTicks = 540)
    public static void lockedSealsMoveFilteredCargoAndResumeAfterReload(GameTestHelper test) {
        var owner = ResearchGameTests.survivalPlayer(test);
        var visitor = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        buildWorkshop(test);

        var emptySeal = placeSeal(test, owner, SOURCE, new ItemStack(TTItems.SEAL_EMPTY_ADVANCED.get()));
        emptySeal.setSetting(EmptyBehavior.LEAVE_ONE, true);
        emptySeal.setPriority((byte) 3);
        configureFilter(emptySeal);
        emptySeal.markChanged(level);
        test.assertTrue(emptySeal.setting(EmptyBehavior.LEAVE_ONE), "The source seal did not enable its reserve");

        var fillSeal = placeSeal(test, owner, TARGET, new ItemStack(TTItems.SEAL_FILL.get()));
        var destinationFilter = configureFilter(fillSeal);
        destinationFilter.setLimit(0, 3);
        fillSeal.markChanged(level);

        test.assertTrue(SealAccess.mayEdit(owner, emptySeal) && !SealAccess.mayEdit(visitor, emptySeal),
                "The locked seal admitted a different owner");
        var outsider = createWorker(test, visitor);
        var worker = createWorker(test, owner);
        test.assertTrue(worker.isWithinRestriction(test.absolutePos(SOURCE)),
                "The worker's home does not cover the source in the test room");
        test.assertTrue(level.addFreshEntity(outsider), "Could not add the visiting golem");
        Vec3 startingSpot = worker.position();
        var workerId = worker.getUUID();

        test.startSequence().thenWaitUntil(() -> {
            test.assertTrue(TaskBoard.of(level).openBlockTasks(outsider.getUUID(), outsider).stream()
                            .anyMatch(task -> emptySeal.pos().equals(task.origin())),
                    "Waiting for the source seal to publish its first task");
        }).thenIdle(28).thenExecute(() -> {
            test.assertTrue(outsider.getTask() == null && outsider.getMainHandItem().isEmpty()
                            && chest(test, SOURCE).countItem(Items.IRON_INGOT) == 6
                            && chest(test, TARGET).isEmpty(),
                    "A visiting golem claimed or completed the owner's task");
            outsider.discard();
            test.assertTrue(level.addFreshEntity(worker), "Could not add the owner's golem");
        }).thenWaitUntil(() -> {
            test.assertTrue(chest(test, TARGET).countItem(Items.IRON_INGOT) == 3
                            && worker.getMainHandItem().is(Items.IRON_INGOT)
                            && worker.getMainHandItem().getCount() == 2,
                    "Waiting for a delivery to the limit of three. Source: "
                            + chest(test, SOURCE).countItem(Items.IRON_INGOT) + ", destination: "
                            + chest(test, TARGET).countItem(Items.IRON_INGOT) + ", held: " + worker.getMainHandItem()
                            + ", worker: " + worker.blockPosition() + ", job: "
                            + (worker.getTask() == null ? "none" : worker.getTask().pos()));
        }).thenExecute(() -> {
            test.assertTrue(worker.distanceToSqr(startingSpot) > 4, "The worker did not navigate to the inventories");
            assertPreservedSource(test);
            reloadSeals(test, emptySeal, fillSeal, owner);
            reloadWorker(test, worker, owner);
        }).thenIdle(32).thenExecute(() -> {
            var restoredWorker = (EntityThaumaturgeGolem) level.getEntity(workerId);
            test.assertTrue(restoredWorker != null && restoredWorker.getMainHandItem().getCount() == 2
                            && chest(test, TARGET).countItem(Items.IRON_INGOT) == 3,
                    "Reloading reset the destination limit or lost the in-flight cargo");
            var restoredFill = Objects.requireNonNull(SealHandler.getSealEntity(level, fillSeal.pos()));
            restoredFill.filter().orElseThrow().setLimit(0, 5);
            restoredFill.markChanged(level);
        }).thenWaitUntil(() -> {
            var restoredWorker = (EntityThaumaturgeGolem) level.getEntity(workerId);
            test.assertTrue(restoredWorker != null && restoredWorker.getMainHandItem().isEmpty()
                            && chest(test, TARGET).countItem(Items.IRON_INGOT) == 5,
                    "Waiting for the restored golem to deliver its remaining cargo");
        }).thenExecute(() -> {
            assertPreservedSource(test);
            SealHandler.removeSealEntity(level, emptySeal.pos(), true);
            SealHandler.removeSealEntity(level, fillSeal.pos(), true);
            Objects.requireNonNull(level.getEntity(workerId)).discard();
        }).thenSucceed();
    }

    private static void buildWorkshop(GameTestHelper test) {
        buildFloor(test);
        for (var pos : new BlockPos[] {SOURCE, TARGET}) {
            SealHandler.removeSealEntity(test.getLevel(), new SealPos(test.absolutePos(pos), FACE), true);
            test.setBlock(pos, Blocks.CHEST);
            chest(test, pos).clearContent();
        }
        chest(test, SOURCE).setItem(0, new ItemStack(Items.IRON_INGOT, 6));
        chest(test, SOURCE).setItem(1, new ItemStack(Items.CARROT, 3));
    }

    private static void buildFloor(GameTestHelper test) {
        for (BlockPos floor : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(8, 1, 8))) {
            test.setBlock(floor, Blocks.SMOOTH_STONE);
        }
    }

    private static SealEntity buildHarvestPlot(GameTestHelper test, FakePlayer owner) {
        buildFloor(test);
        test.setBlock(HARVEST_SEAL, Blocks.SMOOTH_STONE);
        test.setBlock(HARVEST_CROP.below(), Blocks.SOUL_SAND);
        test.setBlock(HARVEST_CROP, Blocks.NETHER_WART.defaultBlockState()
                .setValue(NetherWartBlock.AGE, NetherWartBlock.MAX_AGE));
        var level = test.getLevel();
        SealHandler.removeSealEntity(level, new SealPos(test.absolutePos(HARVEST_SEAL), FACE), true);
        var harvest = placeSeal(test, owner, HARVEST_SEAL, new ItemStack(TTItems.SEAL_HARVEST.get()));
        harvest.setArea(new BlockPos(1, 1, 1));
        harvest.setLocked(true);
        harvest.markChanged(level);
        test.assertTrue(harvest.setting(HarvestBehavior.REPLANT), "Harvest seals should default to replanting");
        return harvest;
    }

    private static List<SupplyOrder> seedRequests(GameTestHelper test, SealEntity harvest) {
        return GolemDispatch.supplyQueue(test.getLevel()).stream()
                .filter(order -> harvest.equals(order.requestingSeal()) && order.wanted().is(Items.NETHER_WART))
                .toList();
    }

    private static SealEntity placeSeal(GameTestHelper test, FakePlayer owner, BlockPos pos, ItemStack seal) {
        var worldPos = test.absolutePos(pos);
        var hit = new BlockHitResult(Vec3.atCenterOf(worldPos).relative(FACE, 0.5), FACE, worldPos, false);
        owner.setItemInHand(InteractionHand.MAIN_HAND, seal);
        var use = new UseOnContext(owner, InteractionHand.MAIN_HAND, hit);
        test.assertTrue(seal.getItem().onItemUseFirst(seal, use).consumesAction() && seal.isEmpty(),
                "The survival seal item was not consumed at " + pos);
        var placed = SealHandler.getSealEntity(test.getLevel(), new SealPos(worldPos, FACE));
        test.assertTrue(placed != null && owner.getUUID().equals(placed.owner()), "Seal placement lost its owner");
        return placed;
    }

    private static ISealFilter configureFilter(SealEntity seal) {
        seal.setLocked(true);
        var filter = seal.filter().orElseThrow();
        filter.setBlacklist(false);
        filter.setStack(0, new ItemStack(Items.IRON_INGOT));
        return filter;
    }

    private static EntityThaumaturgeGolem createWorker(GameTestHelper test, FakePlayer owner) {
        var worker = Objects.requireNonNull(TTEntities.THAUMATURGE_GOLEM.get().create(test.getLevel()));
        Vec3 spawn = Vec3.atBottomCenterOf(test.absolutePos(SPAWN));
        worker.moveTo(spawn.x, spawn.y, spawn.z);
        worker.setValidSpawn();
        worker.setOwner(owner);
        worker.applyBuild(GolemProperties.createDefault().withHead(TTGolemParts.HEAD_SMART.get()));
        ForgeEventFactory.onFinalizeSpawn(worker, test.getLevel(),
                test.getLevel().getCurrentDifficultyAt(worker.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
        return worker;
    }

    private static void reloadSeals(GameTestHelper test, SealEntity emptySeal, SealEntity fillSeal, FakePlayer owner) {
        var level = test.getLevel();
        Stream.of(emptySeal.pos(), fillSeal.pos())
                .map(seal -> level.getChunkAt(seal.pos())).distinct().forEach(chunk -> {
                    var saved = chunk.writeCapsToNBT();
                    SealHandler.unloadChunkSeals(level, chunk);
                    chunk.readCapsFromNBT(saved);
                    SealHandler.loadChunkSeals(level, chunk);
                });
        var restoredEmpty = SealHandler.getSealEntity(level, emptySeal.pos());
        var restoredFill = SealHandler.getSealEntity(level, fillSeal.pos());
        test.assertTrue(restoredEmpty != emptySeal && restoredFill != fillSeal
                        && hasLockedIronFilter(restoredEmpty, owner) && hasLockedIronFilter(restoredFill, owner)
                        && restoredEmpty.setting(EmptyBehavior.LEAVE_ONE) && restoredEmpty.priority() == 3
                        && restoredFill.filter().orElseThrow().limit(0) == 3,
                "Forge chunk capability NBT did not restore both seals and settings");
    }

    private static boolean hasLockedIronFilter(SealEntity seal, FakePlayer owner) {
        return seal != null && seal.isLocked() && owner.getUUID().equals(seal.owner())
                && seal.filter().filter(filter -> !filter.isBlacklist() && filter.stack(0).is(Items.IRON_INGOT))
                        .isPresent();
    }

    private static void reloadWorker(GameTestHelper test, EntityThaumaturgeGolem worker, FakePlayer owner) {
        var saved = new CompoundTag();
        test.assertTrue(worker.save(saved), "Could not save the worker and its cargo");
        worker.discard();
        var restored = createWorker(test, owner);
        restored.load(saved);
        test.assertTrue(worker.getUUID().equals(restored.getUUID()) && owner.getUUID().equals(restored.getOwnerUUID())
                        && worker.getRankXp() == 1 && restored.getRankXp() == 1
                        && restored.getMainHandItem().is(Items.IRON_INGOT)
                        && restored.getMainHandItem().getCount() == 2,
                "Entity NBT did not restore the same golem and its carried ingots");
        test.assertTrue(test.getLevel().addFreshEntity(restored), "The world refused to add the restored golem");
    }

    private static void assertPreservedSource(GameTestHelper test) {
        test.assertTrue(chest(test, SOURCE).countItem(Items.IRON_INGOT) == 1
                        && chest(test, SOURCE).countItem(Items.CARROT) == 3
                        && chest(test, TARGET).countItem(Items.CARROT) == 0,
                "The golem ignored the whitelist or the one-item reserve");
    }

    private static ChestBlockEntity chest(GameTestHelper test, BlockPos pos) {
        return (ChestBlockEntity) test.getBlockEntity(pos);
    }
}
