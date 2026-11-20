package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class WarpCleansingGameTests {
    private WarpCleansingGameTests() {}

    @GameTest(template = "golem_room", batch = "bath_salts", timeoutTicks = 300)
    public static void bathSaltsDissolveAndGrantOneWard(GameTestHelper test) {
        SealAutomationGameTests.prepareRoom(test);
        var bath = new BlockPos(3, 2, 4);
        var dry = bath.east(3);
        var level = test.getLevel();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            test.setBlock(bath.relative(side), Blocks.SMOOTH_STONE);
        }
        test.setBlock(bath, Blocks.WATER);
        var wetSalts = dropSalts(test, bath);
        var drySalts = dropSalts(test, dry);
        test.assertTrue(wetSalts.lifespan == 200 && drySalts.lifespan == 200,
                "Both native item entities should receive the bath salts lifetime");

        test.startSequence().thenIdle(160).thenExecute(() -> {
            test.assertTrue(wetSalts.isAlive() && drySalts.isAlive()
                            && test.getBlockState(bath).is(Blocks.WATER)
                            && test.getBlockState(bath).getFluidState().isSource(),
                    "Bath salts should remain present and leave the water intact until dissolution");
        }).thenWaitUntil(() -> {
            test.assertTrue(test.getBlockState(bath).is(TTBlocks.PURIFYING_FLUID.get())
                            && wetSalts.isRemoved() && drySalts.isRemoved(),
                    "Waiting for the salts to expire and convert the source to Purifying Fluid");
        }).thenExecute(() -> {
            test.assertTrue(test.getBlockState(dry).isAir(), "Dry salts should expire without converting a block");
            var player = ResearchGameTests.survivalPlayer(test);
            var warp = WarpHelper.getWarp(player);
            warp.set(WarpType.PERMANENT, 100);
            var pos = test.absolutePos(bath);
            player.moveTo(Vec3.atBottomCenterOf(pos));
            level.getBlockState(pos).entityInside(level, pos, player);
            var ward = player.getEffect(TTMobEffects.WARP_WARD.get());
            test.assertTrue(ward != null && ward.getDuration() == 20000 && level.getBlockState(pos).isAir(),
                    "Bathing should trade one source for a Warp Ward scaled by permanent Warp");
            test.setBlock(bath, TTBlocks.PURIFYING_FLUID.get());
            level.getBlockState(pos).entityInside(level, pos, player);
            test.assertTrue(level.getBlockState(pos).is(TTBlocks.PURIFYING_FLUID.get())
                            && player.getEffect(TTMobEffects.WARP_WARD.get()) == ward && ward.getDuration() == 20000,
                    "An existing ward should leave the next purifying source available");

            warp.set(WarpType.TEMPORARY, 5);
            player.tickCount = 2000;
            ForgeEventFactory.onPlayerPostTick(player);
            test.assertTrue(warp.get(WarpType.TEMPORARY) == 5,
                    "Warp Ward should suppress the scheduled Warp check");
            player.removeEffect(TTMobEffects.WARP_WARD.get());
            ForgeEventFactory.onPlayerPostTick(player);
            test.assertTrue(warp.get(WarpType.TEMPORARY) == 4,
                    "The scheduled Warp check should resume when the ward clears");
        }).thenSucceed();
    }

    private static ItemEntity dropSalts(GameTestHelper test, BlockPos pos) {
        var level = test.getLevel();
        var at = Vec3.atBottomCenterOf(test.absolutePos(pos)).add(0, 0.2, 0);
        var salts = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(TTItems.BATH_SALTS.get()), 0, 0, 0);
        salts.setNeverPickUp();
        test.assertTrue(level.addFreshEntity(salts), "Could not place the bath salts test item");
        return salts;
    }
}
