package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.BlockEntityEtherealBloom;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintOrigins;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class TaintEcologyGameTests {
    private static final BlockPos BLOOM = new BlockPos(4, 2, 4);

    private TaintEcologyGameTests() {}

    @GameTest(template = "golem_room", batch = "bloom_wakeup", timeoutTicks = 1350)
    public static void sleepingBloomWakesForOffCenterTaint(GameTestHelper test) {
        var level = test.getLevel();
        var origin = test.absolutePos(BLOOM);
        var target = List.of(origin.north(4), origin.south(4), origin.west(4), origin.east(4)).stream()
                .filter(pos -> !sameColumn(pos, new ChunkPos(pos).getMiddleBlockPosition(pos.getY())))
                .filter(pos -> !TaintBiomeManager.isTainted(level, pos))
                .findFirst().orElseThrow();
        var original = noiseBiome(level, target);
        var neighbor = origin;
        var neighborBiome = noiseBiome(level, neighbor);
        int edge = TaintBlooms.PROTECTION_RADIUS;
        for (int dx : new int[] {-edge, edge}) {
            for (int dz : new int[] {-edge, edge}) {
                TaintEcology.setSaturation(level, origin.offset(dx, 0, dz), 0.0F);
            }
        }
        test.assertTrue(TaintBiomeManager.replaceColumn(level, target, TTBiomes.TAINTED_LANDS)
                        && TaintBiomeManager.isChangedColumn(level, target),
                "The native biome writer should mark the converted column");

        var bloom = placeBloom(test);
        sleepUntilNextRecheck(bloom);
        test.startSequence().thenIdle(2).thenExecute(() -> {
            test.assertTrue(!bloom.saveWithoutMetadata().getBoolean("Sleeping"),
                    "A dormant Bloom must wake for an off-center tainted column even when pressure is zero");
        }).thenWaitUntil(() -> test.assertTrue(!TaintBiomeManager.isTainted(level, target),
                "Waiting for the Bloom to sample and restore the tainted column")).thenExecute(() -> {
            test.assertTrue(noiseBiome(level, target).equals(original)
                            && noiseBiome(level, neighbor).equals(neighborBiome)
                            && !TaintBiomeManager.isChangedColumn(level, target),
                    "Cleanup should restore the native biome and clear its marker while preserving the adjacent column");
            test.setBlock(BLOOM.east(), TTBlocks.TAINT_SOIL.get());
            sleepUntilNextRecheck(bloom);
        }).thenIdle(2).thenExecute(() -> {
            test.assertTrue(!bloom.saveWithoutMetadata().getBoolean("Sleeping"),
                    "A dormant Bloom must also wake for a lone tainted block in an otherwise clean column");
            test.setBlock(BLOOM.east(), Blocks.AIR);
            test.setBlock(BLOOM, Blocks.AIR);
        }).thenSucceed();
    }

    @GameTest(template = "golem_room", batch = "bloom_seed", timeoutTicks = 320)
    public static void bloomStarvesAnActiveSeedAndRemovesItsLocalPressure(GameTestHelper test) {
        var level = test.getLevel();
        var seedSite = BLOOM.west(3);
        var at = test.absolutePos(seedSite);
        var bloomPos = test.absolutePos(BLOOM);
        for (var pos : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(8, 5, 8))) {
            test.setBlock(pos, pos.getY() == 1 || pos.getY() == 5 ? Blocks.SMOOTH_STONE : Blocks.AIR);
        }
        TaintEcology.setSaturation(level, at, 0.0F);
        AuraHelper.addFlux(level, at, 12.0F);
        var seed = test.spawn(TTEntities.TAINT_SEED.get(), seedSite);
        seed.setTaintReserve(TaintOrigins.growthCost());

        test.startSequence().thenWaitUntil(() -> test.assertTrue(
                seed.getTaintReserve() > TaintOrigins.growthCost() && TaintEcology.getSaturation(level, at) > 0.0F,
                "Waiting for the active seed to feed and add ecological pressure")).thenExecute(() -> {
            placeBloom(test);
            test.assertTrue(TaintBlooms.isProtected(level, at) && TaintBlooms.isProtected(level, bloomPos.east(8))
                            && !TaintBlooms.isProtected(level, bloomPos.east(9)),
                    "The planted Bloom should register protection through eight blocks");
        }).thenWaitUntil(() -> test.assertTrue(seed.getHealth() < seed.getMaxHealth()
                        && TaintEcology.getSaturation(level, at) == 0.0F,
                "Waiting for Bloom protection to starve the seed and remove its local pressure")).thenExecute(() -> {
            test.assertTrue(AuraHelper.getFlux(level, at) > 0.0F, "The Bloom should leave flux in the aura");
            seed.discard();
            test.setBlock(BLOOM, Blocks.AIR);
            test.assertTrue(!TaintBlooms.isProtected(level, at), "Removing the Bloom should remove its protection");
        }).thenSucceed();
    }

    private static BlockEntityEtherealBloom placeBloom(GameTestHelper test) {
        test.setBlock(BLOOM.below(), Blocks.SMOOTH_STONE);
        test.setBlock(BLOOM, TTBlocks.ETHEREAL_BLOOM.get());
        return (BlockEntityEtherealBloom) test.getBlockEntity(BLOOM);
    }

    private static void sleepUntilNextRecheck(BlockEntityEtherealBloom bloom) {
        var saved = bloom.saveWithoutMetadata();
        saved.putBoolean("Sleeping", true);
        saved.putInt("Ticks", 1199);
        bloom.load(saved);
    }

    private static Holder<Biome> noiseBiome(ServerLevel level, BlockPos pos) {
        return level.getNoiseBiome(
                QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()));
    }

    private static boolean sameColumn(BlockPos first, BlockPos second) {
        return QuartPos.fromBlock(first.getX()) == QuartPos.fromBlock(second.getX())
                && QuartPos.fromBlock(first.getZ()) == QuartPos.fromBlock(second.getZ());
    }
}
