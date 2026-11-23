package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityStabilizer;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.infusion.instability.EjectAftermath;
import com.leclowndu93150.thaumaturge.content.infusion.instability.EjectEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.InstabilityContext;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class StabilizerGameTests {
    private static final BlockPos STABILIZER = new BlockPos(2, 2, 4);
    private static final BlockPos COMPARATOR = STABILIZER.west();
    private static final BlockPos INLAY = STABILIZER.east();
    private static final BlockPos PEDESTAL = INLAY.east(2);

    private StabilizerGameTests() {}

    @GameTest(template = "golem_room", batch = "stabilizer_inlay", timeoutTicks = 220)
    public static void chargedInlaysProtectPedestalAfterStabilizerReload(GameTestHelper test) {
        SealAutomationGameTests.prepareRoom(test);
        var level = test.getLevel();
        var source = test.absolutePos(STABILIZER);
        var target = test.absolutePos(PEDESTAL);
        test.setBlock(STABILIZER, TTBlocks.STABILIZER.get());
        test.setBlock(COMPARATOR, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.EAST));
        test.setBlock(INLAY, TTBlocks.INLAY.get());
        test.setBlock(INLAY.east(), TTBlocks.INLAY.get());
        test.setBlock(PEDESTAL, TTBlocks.PEDESTAL_ARCANE.get());
        var pedestal = (BlockEntityPedestal) test.getBlockEntity(PEDESTAL);
        var original = (BlockEntityStabilizer) test.getBlockEntity(STABILIZER);
        var comparator = (ComparatorBlockEntity) test.getBlockEntity(COMPARATOR);
        var ingredient = new ItemStack(Items.AMETHYST_SHARD).setHoverName(Component.literal("Warded ingredient"));
        pedestal.setItem(ingredient.copy());

        test.startSequence().thenWaitUntil(() -> test.assertTrue(original.charge() == 7 && comparator.getOutputSignal() == 7,
                "Waiting for the Stabilizer and vanilla comparator to reach seven")).thenExecute(() -> {
            test.assertTrue(chargeAt(test, INLAY) == 7 && chargeAt(test, INLAY.east()) == 6
                            && chargeAt(test, PEDESTAL) == 5 && source.equals(pedestal.findInstabilityMitigator()),
                    "Charge should reach the stocked pedestal through two inlay segments");
            var saved = original.saveWithFullMetadata();
            var restored = new BlockEntityStabilizer(source, level.getBlockState(source));
            level.removeBlockEntity(source);
            level.setBlockEntity(restored);
            restored.load(saved);
            test.assertTrue(original.isRemoved() && restored.charge() == 7 && source.equals(pedestal.findInstabilityMitigator()),
                    "Native block-entity reload should keep stored charge and the ward connection");

            var instability = new EjectEffect(false, EjectAftermath.NONE, ConstantInt.of(5), ConstantInt.of(0), 0);
            var context = new InstabilityContext(level, target.above(), List.of(target));
            instability.apply(context);
            test.assertTrue(ItemStack.matches(ingredient, pedestal.getItem()) && restored.charge() == 2,
                    "One successful ward should keep the exact ingredient and spend five charge");
            test.assertTrue(chargeAt(test, INLAY) == 2 && chargeAt(test, INLAY.east()) == 1 && chargeAt(test, PEDESTAL) == 0,
                    "The lower charge should propagate back through the whole inlay line");

            instability.apply(context);
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(2),
                    item -> ItemStack.matches(ingredient, item.getItem()));
            test.assertTrue(pedestal.getItem().isEmpty() && drops.size() == 1 && restored.charge() == 2,
                    "An undercharged ward should leave charge intact and eject exactly the original ingredient");
        }).thenWaitUntil(() -> test.assertTrue(comparator.getOutputSignal() == 2,
                "Waiting for the comparator to report the charge spent on the ward")).thenSucceed();
    }

    @GameTest(template = "golem_room", batch = "stabilizer_rift", timeoutTicks = 80)
    public static void stabilizerRechargesThenSteadiesOnlyTheNearbyRift(GameTestHelper test) {
        SealAutomationGameTests.prepareRoom(test);
        var level = test.getLevel();
        var source = test.absolutePos(STABILIZER);
        test.setBlock(STABILIZER, TTBlocks.STABILIZER.get());
        var stabilizer = (BlockEntityStabilizer) test.getBlockEntity(STABILIZER);
        var nearby = placeRift(test, STABILIZER.east(4).above(), 25);
        var veryStable = placeRift(test, STABILIZER.east(4).south(2).above(), 75);
        var distant = placeRift(test, STABILIZER.east(4).above(16), 25);
        var aura = Objects.requireNonNull(AuraManager.chunkAt(level, source));
        short oldBase = aura.getBase();
        float oldFlux = aura.getFlux();
        aura.setBase((short) 0);
        aura.setFlux(0);

        test.startSequence().thenWaitUntil(() -> test.assertTrue(nearby.stabilityValue() > 25,
                "Waiting for a charged Stabilizer to improve the nearby rift")).thenExecute(() -> {
            test.assertTrue(nearby.stabilityValue() == 25.125F
                            && veryStable.stabilityValue() == 75 && distant.stabilityValue() == 25,
                    "One pulse should improve the nearby rift and leave both controls alone");
            test.assertTrue(stabilizer.charge() == 0 && AuraHelper.getFlux(level, source) == 0.25F,
                    "Charging should produce flux and the first pulse should spend the generated charge");
            nearby.discard();
            veryStable.discard();
            distant.discard();
        }).thenIdle(25).thenExecute(() -> {
            aura.setBase(oldBase);
            aura.setFlux(oldFlux);
            test.assertTrue(stabilizer.charge() == 1,
                    "The device should retain its next charge after the rifts are removed");
        }).thenSucceed();
    }

    private static int chargeAt(GameTestHelper test, BlockPos pos) {
        return BlockInlay.chargeAt(test.getLevel(), test.absolutePos(pos));
    }

    private static EntityFluxRift placeRift(GameTestHelper test, BlockPos pos, int stability) {
        var level = test.getLevel();
        var rift = Objects.requireNonNull(TTEntities.FLUX_RIFT.get().create(level));
        rift.moveTo(Vec3.atCenterOf(test.absolutePos(pos)));
        rift.reseed(46786);
        rift.resize(16);
        rift.adjustStability(stability);
        test.assertTrue(level.addFreshEntity(rift), "Could not place the rift for the Stabilizer");
        return rift;
    }
}
