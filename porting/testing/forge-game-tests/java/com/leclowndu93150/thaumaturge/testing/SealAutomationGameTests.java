package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.EmptyBehavior;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
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
    private static final Direction FACE = Direction.NORTH;

    private SealAutomationGameTests() {}

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
        for (BlockPos floor : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(8, 1, 8))) {
            test.setBlock(floor, Blocks.SMOOTH_STONE);
        }
        for (var pos : new BlockPos[] {SOURCE, TARGET}) {
            SealHandler.removeSealEntity(test.getLevel(), new SealPos(test.absolutePos(pos), FACE), true);
            test.setBlock(pos, Blocks.CHEST);
            chest(test, pos).clearContent();
        }
        chest(test, SOURCE).setItem(0, new ItemStack(Items.IRON_INGOT, 6));
        chest(test, SOURCE).setItem(1, new ItemStack(Items.CARROT, 3));
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
