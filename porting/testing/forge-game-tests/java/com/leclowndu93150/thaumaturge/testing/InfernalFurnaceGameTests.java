package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class InfernalFurnaceGameTests {
    private InfernalFurnaceGameTests() {}

    @GameTest(template = "essentia_room", timeoutTicks = 360)
    public static void stationaryDropSmeltsIntoChest(GameTestHelper test) {
        var core = new BlockPos(2, 1, 1);
        var output = core.south(2);
        test.setBlock(core, TTBlocks.INFERNAL_FURNACE.get());
        test.setBlock(output, Blocks.CHEST);
        var furnace = (BlockEntityInfernalFurnace) test.getBlockEntity(core);
        var chest = (ChestBlockEntity) test.getBlockEntity(output);

        var level = test.getLevel();
        var position = Vec3.atBottomCenterOf(test.absolutePos(core)).add(0, 0.5, 0);
        // With an odd entity ID, a stationary item's collision callbacks miss every tenth item tick.
        ItemEntity candidate;
        do {
            candidate = new ItemEntity(level, position.x, position.y, position.z, new ItemStack(Items.COBBLESTONE, 2));
        } while (candidate.getId() % 2 == 0);
        var dropped = candidate;
        dropped.setDeltaMovement(Vec3.ZERO);
        dropped.setOnGround(true);
        level.addFreshEntity(dropped);

        test.startSequence().thenWaitUntil(() -> {
            var input = furnace.inventory().getStackInSlot(0);
            test.assertTrue(dropped.isRemoved() && input.is(Items.COBBLESTONE) && input.getCount() == 2,
                    "Waiting for the furnace to collect both cobblestone from the stationary item");
        }).thenWaitUntil(() -> {
            var result = chest.getItem(0);
            test.assertTrue(result.is(Items.STONE) && result.getCount() == 2,
                    "Waiting for both stone blocks to reach the chest");
            test.assertTrue(furnace.inventory().getStackInSlot(0).isEmpty(),
                    "The furnace kept a copy of the smelted input");
        }).thenSucceed();
    }
}
