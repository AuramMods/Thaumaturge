package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class FluxRiftGameTests {
    private static final String RIFT_PRESSURE_BATCH = "rift_pressure";
    private static boolean pressureEventsEnabled;

    private FluxRiftGameTests() {}

    @BeforeBatch(batch = RIFT_PRESSURE_BATCH)
    public static void isolateRiftFluxCost(ServerLevel level) {
        pressureEventsEnabled = ThaumaturgeCommonConfig.FLUX_PRESSURE_EVENTS.get();
        // Other outbreaks spend from the same aura during the spawn tick.
        ThaumaturgeCommonConfig.FLUX_PRESSURE_EVENTS.set(false);
    }

    @AfterBatch(batch = RIFT_PRESSURE_BATCH)
    public static void restorePressureEvents(ServerLevel level) {
        ThaumaturgeCommonConfig.FLUX_PRESSURE_EVENTS.set(pressureEventsEnabled);
    }

    @GameTest(template = "golem_room", batch = RIFT_PRESSURE_BATCH, timeoutTicks = 1950)
    public static void overloadedAuraSpawnsOneRiftAndPaysItsFlux(GameTestHelper test) {
        var level = test.getLevel();
        var chunk = new ChunkPos(test.absolutePos(new BlockPos(4, 2, 4)));
        var corner = new BlockPos(chunk.getMinBlockX(), 110, chunk.getMinBlockZ());
        var farCorner = new BlockPos(chunk.getMaxBlockX(), 110, chunk.getMaxBlockZ());
        var site = new AABB(corner.above(), farCorner.offset(1, level.getMaxBuildHeight() - corner.getY(), 1));
        for (BlockPos pos : BlockPos.betweenClosed(corner, farCorner)) {
            level.setBlockAndUpdate(pos, Blocks.BEDROCK.defaultBlockState());
        }
        float startingFlux = AuraHelper.getFlux(level, corner);
        AuraHelper.addFlux(level, corner, 900);
        var previousFlux = new AtomicReference<>(AuraHelper.getFlux(level, corner));
        var spawned = new AtomicReference<EntityFluxRift>();
        test.startSequence().thenIdle(1700).thenExecute(() -> {
            test.assertTrue(level.getEntitiesOfClass(EntityFluxRift.class, site).isEmpty(),
                    "Overload should build pressure before creating a rift");
            // Once the buildup is nearly complete, make the native rift roll certain.
            AuraHelper.addFlux(level, corner, 5500);
        }).thenWaitUntil(() -> {
            var rifts = level.getEntitiesOfClass(EntityFluxRift.class, site);
            if (rifts.isEmpty()) {
                previousFlux.set(AuraHelper.getFlux(level, corner));
            }
            test.assertTrue(rifts.size() == 1, "Waiting for the native aura tick to create a rift");
            spawned.set(rifts.get(0));
        }).thenExecute(() -> {
            var rift = spawned.get();
            var pos = rift.blockPosition();
            float spent = previousFlux.get() - AuraHelper.getFlux(level, corner);
            test.assertTrue(chunk.equals(new ChunkPos(pos)) && rift.shapeSeed() != 0
                            && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).equals(pos),
                    "The rift should spawn seeded above the local surface");
            test.assertTrue(Math.abs(spent - rift.currentSize()) <= 2,
                    "The rift should spend its starting size in flux. Before: " + previousFlux.get()
                            + ", after: " + AuraHelper.getFlux(level, corner) + ", size: " + rift.currentSize());
        }).thenIdle(45).thenExecute(() -> {
            test.assertTrue(level.getEntitiesOfClass(EntityFluxRift.class, site.inflate(32)).equals(List.of(spawned.get())),
                    "Continued overload should not create a second nearby rift");
            spawned.get().discard();
            AuraHelper.drainFlux(level, corner, AuraHelper.getFlux(level, corner) - startingFlux, false);
        }).thenSucceed();
    }

    @GameTest(template = "golem_room", batch = "rift_collapse", timeoutTicks = 120)
    public static void thrownCollapserClosesRiftAcrossReload(GameTestHelper test) {
        SealAutomationGameTests.prepareRoom(test);
        var level = test.getLevel();
        var riftPos = Vec3.atCenterOf(test.absolutePos(new BlockPos(4, 3, 6)));
        var rift = Objects.requireNonNull(TTEntities.FLUX_RIFT.get().create(level));
        rift.moveTo(riftPos);
        rift.reseed(46786);
        rift.resize(16);
        rift.adjustStability(75);
        test.assertTrue(level.addFreshEntity(rift), "Could not place the active rift");

        var player = ResearchGameTests.survivalPlayer(test);
        var stand = Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(4, 2, 1)));
        player.moveTo(stand.x, stand.y, stand.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, riftPos);
        var charges = new ItemStack(TTItems.CAUSALITY_COLLAPSER.get(), 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, charges);
        test.assertTrue(charges.use(level, player, InteractionHand.MAIN_HAND).getResult().consumesAction()
                        && charges.getCount() == 1,
                "Survival use should throw and spend one collapser");

        var saved = new CompoundTag();
        var restored = Objects.requireNonNull(TTEntities.FLUX_RIFT.get().create(level));
        test.startSequence().thenWaitUntil(() -> test.assertTrue(rift.isCollapsing(),
                "Waiting for the thrown collapser to hit the rift in a clear lane")).thenExecute(() -> {
            test.assertTrue(rift.collapseSize() == 16 && rift.save(saved), "The triggered collapse should save its original size");
            rift.discard();
        }).thenIdle(1).thenExecute(() -> {
            restored.load(saved);
            test.assertTrue(restored.getUUID().equals(rift.getUUID()) && restored.isCollapsing()
                            && restored.currentSize() == rift.currentSize() && restored.collapseSize() == 16
                            && restored.shapeSeed() == 46786 && restored.stabilityValue() == 75,
                    "Native NBT should keep the rift identity, collapse and shape");
            test.assertTrue(level.addFreshEntity(restored), "Could not restore the collapsing rift");
        }).thenWaitUntil(() -> test.assertTrue(restored.isRemoved(), "Waiting for the restored rift to finish collapsing"))
                .thenExecute(() -> {
                    var drops = new AABB(riftPos, riftPos).inflate(5);
                    int seeds = level.getEntitiesOfClass(ItemEntity.class, drops, item -> item.getItem().is(TTItems.VOID_SEED.get()))
                            .stream().mapToInt(item -> item.getItem().getCount()).sum();
                    test.assertTrue(seeds == 4, "One size-16 collapse should drop exactly four Void Seeds");
                }).thenSucceed();
    }
}
