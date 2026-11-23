package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.content.eldritch.altar.BlockEntityEldritchAltar;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.state.AttachmentData;
import com.leclowndu93150.thaumaturge.testing.StageGameTests;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class EldritchStageGameTests {
    private static final BlockPos CORE = new BlockPos(3, 2, 3);
    private static final String SEAT = "seat_eldritch_eyes";
    private static final String OPEN = "open_eldritch_altar";
    private static final String ENTER = "enter_eldritch_labyrinth";

    private EldritchStageGameTests() {}

    @GameTest(template = "golem_room", batch = "crafttweaker_eldritch", timeoutTicks = 360)
    public static void altarResumesPaidRitualAndPortalChecksEntryStages(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var portalPos = pos.above();
        var actor = StageGameTests.player(test);
        Vec3 footing = Vec3.atBottomCenterOf(pos.south(2));
        actor.moveTo(footing.x, footing.y, footing.z);
        var knowledge = (PlayerKnowledge) PlayerResearchAccess.of(actor);
        knowledge.learn(TTIds.RESEARCH_OCULUS);
        knowledge.markComplete(TTIds.RESEARCH_OCULUS);
        var stages = GameStageHelper.getPlayerData(actor);
        var block = TTBlocks.ELDRITCH_ALTAR.get();
        var state = block.defaultBlockState();
        test.setBlock(CORE, state);
        test.setBlock(CORE.above(), TTBlocks.NODE.get());
        var altar = (BlockEntityEldritchAltar) test.getBlockEntity(CORE);
        var eyes = new ItemStack(TTItems.ELDRITCH_EYE.get(), 2);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        block.useItemOn(eyes, state, level, pos, actor, InteractionHand.MAIN_HAND, hit);
        test.assertTrue(altar.getEyes() == 0 && eyes.getCount() == 2, "Eye placement spent a locked offering");
        stages.addStage(SEAT);
        block.useItemOn(eyes, state, level, pos, actor, InteractionHand.MAIN_HAND, hit);
        block.useItemOn(eyes, state, level, pos, actor, InteractionHand.MAIN_HAND, hit);
        test.assertTrue(altar.getEyes() == 2 && eyes.isEmpty(), "The altar did not seat exactly two Eyes");

        var aura = Objects.requireNonNull(AuraManager.chunkAt(level, pos));
        float priorVis = aura.getVis();
        var wand = new ItemStack(TTItems.WAND.get());
        try {
            aura.setVis(12);
            block.onCasterRightClick(level, wand, actor, pos, Direction.UP, InteractionHand.MAIN_HAND);
            altar.serverTick(level, pos);
            test.assertTrue(aura.getVis() == 12 && !altar.saveWithFullMetadata().contains("ritual"),
                    "A restricted altar began its channel or took vis");
            stages.addStage(OPEN);
            block.onCasterRightClick(level, wand, actor, pos, Direction.UP, InteractionHand.MAIN_HAND);
            altar.serverTick(level, pos);
            test.assertTrue(aura.getVis() == 10 && altar.getEyes() == 2,
                    "One scripted ritual step should cost two vis and retain the offerings");
            stages.removeStage(OPEN);
            altar.load(altar.saveWithFullMetadata());
            altar.serverTick(level, pos);
            test.assertTrue(aura.getVis() == 10 && altar.getEyes() == 2,
                    "Revocation advanced or canceled a saved paid channel");
            stages.addStage(OPEN);
            altar.serverTick(level, pos);
            altar.serverTick(level, pos);
            test.assertTrue(aura.getVis() == 6 && altar.getEyes() == 0
                            && level.getBlockEntity(portalPos) instanceof BlockEntityEldritchPortal,
                    "The altar did not spend six vis and two Eyes once to create a portal");
        } finally {
            aura.setVis(priorVis);
        }

        var portal = (BlockEntityEldritchPortal) level.getBlockEntity(portalPos);
        var link = portal.link().orElseThrow();
        LabyrinthTransit.touch(level, actor, portal.link());
        var transit = AttachmentData.get(actor, TTAttachments.LABYRINTH_TRANSIT);
        test.assertTrue(transit.pending().isEmpty(), "The locked portal reserved destination chunks");
        stages.addStage(ENTER);
        LabyrinthTransit.touch(level, actor, portal.link());
        test.assertTrue(transit.pending().isPresent(), "The eligible portal did not queue an entry");
        stages.removeStage(ENTER);
        LabyrinthTransit.tick(actor);
        test.assertTrue(transit.pending().isEmpty() && actor.serverLevel() == level,
                "Revocation did not cancel an already-queued entry");

        stages.addStage(ENTER);
        actor.setGameMode(GameType.CREATIVE);
        Vec3 inside = Vec3.atBottomCenterOf(portalPos);
        actor.moveTo(inside.x, inside.y, inside.z);
        var outer = LabyrinthService.outer(level.getServer()).orElseThrow();
        test.startSequence().thenWaitUntil(() -> {
            if (actor.serverLevel() == level) {
                LabyrinthTransit.touch(level, actor, portal.link());
                LabyrinthTransit.tick(actor);
            }
            test.assertTrue(actor.serverLevel() == outer, "Waiting for labyrinth entry, pending="
                    + transit.pending().isPresent() + ", loaded=" + transit.pending().map(p -> p.loaded().isDone())
                    + ", eligible=" + actor.canChangeDimensions() + ", cooldown=" + actor.isOnPortalCooldown());
        }).thenExecute(() -> {
            StageGameTests.remove(test, actor);
            LabyrinthService.retire(outer, link.maze());
        }).thenSucceed();
    }
}
