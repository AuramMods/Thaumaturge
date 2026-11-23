package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeJarRitual;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Objects;
import java.util.Optional;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class NodeJarringGameTests {
    private static final String STAGE = "jar_aura_nodes";
    private static final BlockPos CORE = new BlockPos(3, 3, 3);
    private static final int HELD = 1500;
    private static final int SCRIPT_VIS = 2;

    private NodeJarringGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_node_jarring", timeoutTicks = 240)
    public static void nodeJarringChargesSixPrimalsAndResumesForItsInitiator(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(CORE);
        var actor = StageGameTests.player(test);
        var other = StageGameTests.player(test);
        Vec3 handSpot = Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(1, 2, 1)));
        actor.moveTo(handSpot.x, handSpot.y, handSpot.z);
        other.moveTo(handSpot.x, handSpot.y, handSpot.z);
        var knowledge = (PlayerKnowledge) PlayerResearchAccess.of(actor);
        knowledge.learn(NodeJarRitual.RESEARCH_NODE_JAR);
        knowledge.markComplete(NodeJarRitual.RESEARCH_NODE_JAR);
        test.setBlock(CORE, TTBlocks.NODE.get());
        var node = (BlockEntityNode) test.getBlockEntity(CORE);
        var aer = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AER));
        var contents = AspectList.EMPTY.add(aer, 8);
        node.applyNodeData(new NodeData(NodeType.NORMAL, Optional.of(NodeModifier.BRIGHT), contents, contents));
        var shell = NodeJarRitual.structurePositions(pos);
        for (BlockPos cell : shell) {
            var material = cell.getY() == pos.getY() + 2 ? Blocks.OAK_SLAB : Blocks.GLASS;
            level.setBlock(cell, material.defaultBlockState(), Block.UPDATE_ALL);
        }
        test.assertTrue(NodeJarRitual.fitsStructure(level, pos), "The node ritual fixture has an invalid shell");

        var wand = new ItemStack(TTItems.WAND.get());
        for (var primal : TTAspects.PRIMALS) {
            WandVisHelper.storeVis(wand, primal, HELD);
        }
        actor.getInventory().setItem(0, wand);
        NodeJarRitual.tryJarNode(level, pos, actor);
        test.assertTrue(!node.isJarring() && TTAspects.PRIMALS.stream()
                        .allMatch(primal -> WandVisHelper.getVis(wand, primal) == HELD),
                "The ritual took payment or started while the requester lacked the stage");

        var stages = GameStageHelper.getPlayerData(actor);
        stages.addStage(STAGE);
        NodeJarRitual.tryJarNode(level, pos, actor);
        test.assertTrue(node.isJarring(), "The eligible actor could not start jarring");
        var paid = WandVisHelper.getAllVis(wand);
        for (var primal : TTAspects.PRIMALS) {
            int owed = (int) (SCRIPT_VIS * WandEconomy.CENTIVIS_PER_VIS
                    * WandVisHelper.getConsumptionModifier(wand, actor, primal, true));
            test.assertTrue(paid.amount(primal) == HELD - owed, "The ritual mischarged " + primal.location());
        }
        stages.removeStage(STAGE);
        GameStageHelper.getPlayerData(other).addStage(STAGE);
        node.load(node.saveWithFullMetadata());
        test.startSequence().thenIdle(45).thenExecute(() -> {
            test.assertTrue(node.isJarring() && NodeJarRitual.fitsStructure(level, pos),
                    "The saved job or its shell advanced with a different eligible player");
            stages.addStage(STAGE);
        }).thenWaitUntil(() -> test.assertTrue(level.getBlockEntity(pos) instanceof BlockEntityJarNode,
                "Waiting for the original actor's restored stage to resume jarring")).thenExecute(() -> {
            var jar = (BlockEntityJarNode) level.getBlockEntity(pos);
            test.assertTrue(jar.getNodeModifier() == NodeModifier.BRIGHT && jar.getAspectsBase().amountOf(aer) == 8,
                    "The jar lost the node's aspects or ignored the scripted modifier rule");
            test.assertTrue(shell.stream().allMatch(level::isEmptyBlock) && WandVisHelper.getAllVis(wand).equals(paid),
                    "The ritual left shell blocks or charged a second time");
            StageGameTests.remove(test, actor);
            StageGameTests.remove(test, other);
        }).thenSucceed();
    }
}
