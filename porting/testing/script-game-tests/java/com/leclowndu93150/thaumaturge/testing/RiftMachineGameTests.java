package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityStabilizer;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVoidSiphon;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class RiftMachineGameTests {
    private static final BlockPos CORE = new BlockPos(3, 2, 3);

    private RiftMachineGameTests() {}

    @GameTest(template = "golem_room", batch = "crafttweaker_rift_machines")
    public static void voidSiphonRetainsASavedRiftPayment(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var block = TTBlocks.VOID_SIPHON.get();
        var state = block.defaultBlockState();
        test.setBlock(CORE, state);
        test.setBlock(CORE.above(), Blocks.AIR);
        test.setBlock(CORE.east().above(), Blocks.AIR);
        test.setBlock(CORE.east(2).above(), Blocks.AIR);
        var siphon = (BlockEntityVoidSiphon) test.getBlockEntity(CORE);
        var rift = placeRift(test, CORE.east(2).above());
        var actor = StageGameTests.player(test);
        try {
            block.setPlacedBy(level, pos, state, actor, new ItemStack(block));
            var stages = GameStageHelper.getPlayerData(actor);
            BlockEntityVoidSiphon.serverTick(level, pos, state, siphon);
            test.assertTrue(siphon.progress() == 0 && rift.stabilityValue() == 25,
                    "A restricted siphon took stability from the rift");
            stages.addStage("siphon_rifts");
            BlockEntityVoidSiphon.serverTick(level, pos, state, siphon);
            test.assertTrue(siphon.progress() == 4 && siphon.progressRequired() == 8 && rift.stabilityValue() == 24,
                    "The scripted siphon did not gain four points for one stability");
            stages.removeStage("siphon_rifts");
            siphon.load(siphon.saveWithFullMetadata());
            BlockEntityVoidSiphon.serverTick(level, pos, state, siphon);
            test.assertTrue(siphon.progress() == 4 && rift.stabilityValue() == 24,
                    "The saved siphon advanced or lost its progress while suspended");
            stages.addStage("siphon_rifts");
            BlockEntityVoidSiphon.serverTick(level, pos, state, siphon);
            stages.removeStage("siphon_rifts");
            var output = siphon.output().extractItem(0, 64, false);
            test.assertTrue(siphon.progress() == 0 && rift.stabilityValue() == 23 && rift.currentSize() == 16
                            && output.is(Items.ECHO_SHARD) && output.getCount() == 2
                            && siphon.output().extractItem(0, 64, false).isEmpty(),
                    "Exactly two echo shards should be recoverable from two configured rift payments");
        } finally {
            rift.discard();
            StageGameTests.remove(test, actor);
        }
        test.succeed();
    }

    @GameTest(template = "golem_room", batch = "crafttweaker_rift_machines")
    public static void stabilizerUsesTheConfiguredRiftRange(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var state = TTBlocks.STABILIZER.get().defaultBlockState();
        test.setBlock(CORE, state);
        var stabilizer = (BlockEntityStabilizer) test.getBlockEntity(CORE);
        var charged = stabilizer.saveWithFullMetadata();
        charged.putInt("energy", 2);
        stabilizer.load(charged);
        var nearby = placeRift(test, CORE.east(4).above());
        var distant = placeRift(test, CORE.east(7).above());
        try {
            for (int tick = 0; tick < 5; tick++) {
                BlockEntityStabilizer.serverTick(level, pos, state, stabilizer);
            }
            test.assertTrue(stabilizer.charge() == 1 && nearby.stabilityValue() == 25.125F
                            && distant.stabilityValue() == 25,
                    "The authored range should spend one charge only on the rift within five blocks");
        } finally {
            nearby.discard();
            distant.discard();
        }
        test.succeed();
    }

    private static EntityFluxRift placeRift(GameTestHelper test, BlockPos relative) {
        var level = test.getLevel();
        var rift = Objects.requireNonNull(TTEntities.FLUX_RIFT.get().create(level));
        rift.moveTo(Vec3.atCenterOf(test.absolutePos(relative)));
        rift.reseed(46786);
        rift.resize(16);
        rift.adjustStability(25);
        test.assertTrue(level.addFreshEntity(rift), "Could not place a fixture flux rift");
        return rift;
    }
}
