package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitEngine;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintInfection;
import com.leclowndu93150.thaumaturge.mixin.world.entity.MobAccessor;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class MobTraitGameTests {
    private MobTraitGameTests() {}

    @GameTest(template = "golem_room", batch = "mob_traits", timeoutTicks = 120)
    public static void pechTamesThroughItsNativeFetchGoal(GameTestHelper test) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(1, 1, 1), new BlockPos(7, 1, 7))) {
            test.setBlock(pos, Blocks.SMOOTH_STONE);
        }
        var pech = test.spawn(TTEntities.PECH.get(), new BlockPos(4, 2, 4));
        pech.setCanPickUpLoot(false);
        var spot = test.absolutePos(new BlockPos(6, 2, 4));
        var level = test.getLevel();
        var pearl = new ItemEntity(level, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5,
                new ItemStack(Items.ENDER_PEARL));
        pearl.setDeltaMovement(0.0, 0.0, 0.0);
        test.assertTrue(level.addFreshEntity(pearl), "Could not offer the Pech an Ender Pearl");

        test.startSequence().thenWaitUntil(() -> test.assertTrue(pech.isDomesticated() && pearl.isRemoved(),
                "Waiting for the Pech to collect the pearl through its native goal"))
                .thenIdle(2).thenExecute(() -> {
                    test.assertTrue(pech.isAlive() && pech.isDomesticated(), "The Pech should keep ticking after taming");
                    pech.discard();
                }).thenSucceed();
    }

    @GameTest(template = "golem_room", batch = "mob_traits", timeoutTicks = 360)
    public static void taintReplacesAndRestoresActiveCowGoals(GameTestHelper test) {
        var level = test.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(8, 5, 8))) {
            boolean enclosure = pos.getY() == 1 || pos.getY() == 5
                    || pos.getX() == 0 || pos.getX() == 8 || pos.getZ() == 0 || pos.getZ() == 8;
            test.setBlock(pos, enclosure ? Blocks.SMOOTH_STONE : Blocks.AIR);
        }
        var cow = test.spawn(EntityType.COW, new BlockPos(4, 2, 4));
        var prey = test.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(2, 2, 4));
        prey.setHealth(1);
        var goals = ((MobAccessor) cow).thaumaturge$getGoalSelector();
        var original = List.copyOf(goals.getAvailableGoals());
        float normalHealth = cow.getMaxHealth();
        var start = cow.position();
        test.assertTrue(cow.hurt(level.damageSources().mobAttack(prey), 1), "Could not start the cow's native panic");

        test.startSequence().thenWaitUntil(() -> test.assertTrue(cow.distanceToSqr(start) > 0.04 && isPanicking(goals),
                "Waiting for the cow to move under its native PanicGoal")).thenExecute(() -> {
            TaintInfection.tryInfect(level, cow);
            test.assertTrue(MobTraits.isTainted(cow) && MobTraitEngine.suppressesNativeAi(cow)
                            && MobTraits.has(cow, TTMobTraits.key(TTMobTraits.FLUX_MILK)) && cow.getMaxHealth() == 40,
                    "The infection should install the registered cow profile and replace native AI");
            test.assertTrue(original.stream().noneMatch(WrappedGoal::isRunning),
                    "Taint replacement left an old native goal running");
        }).thenWaitUntil(() -> test.assertTrue(prey.isDeadOrDying(),
                "Waiting for the infected cow to hunt the nearby sheep")).thenExecute(() -> {
            var taint = TTMobTraits.registry().getHolderOrThrow(MobTraits.TAINTED);
            test.assertTrue(MobTraits.remove(cow, taint) && !MobTraits.isTainted(cow)
                            && !MobTraitEngine.suppressesNativeAi(cow) && cow.getMaxHealth() == normalHealth
                            && !MobTraits.has(cow, TTMobTraits.key(TTMobTraits.FLUX_MILK)),
                    "Removing the infection should restore the native stats and remove the cow profile's trait");
            var restored = goals.getAvailableGoals();
            test.assertTrue(restored.size() == original.size() && original.stream().allMatch(old ->
                            restored.stream().anyMatch(current ->
                                    current.getGoal() == old.getGoal() && current.getPriority() == old.getPriority())),
                    "Removing the trait should restore each original goal at its priority exactly once");
            cow.setSecondsOnFire(5);
        }).thenWaitUntil(() -> test.assertTrue(isPanicking(goals), "Waiting for the restored cow to panic"))
                .thenExecute(cow::discard).thenSucceed();
    }

    private static boolean isPanicking(GoalSelector selector) {
        return selector.getRunningGoals().anyMatch(goal -> goal.getGoal() instanceof PanicGoal);
    }
}
