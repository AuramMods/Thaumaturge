package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class AssemblyGameTests {
    private static final BlockPos CORE = new BlockPos(2, 2, 2);
    private static final String BLUEPRINT_STAGE = "build_rotated_frame";

    private AssemblyGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_assembly")
    public static void editedGeneratedWandKeepsItsPartsAndCraftCost(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var knowledge = (PlayerKnowledge) PlayerResearchAccess.of(actor);
        var unlock = TTIds.rl("unlock_auromancy");
        knowledge.learn(unlock);
        knowledge.markComplete(unlock);
        test.setBlock(CORE, TTBlocks.ARCANE_WORKBENCH.get());
        var workbench = (BlockEntityArcaneWorkbench) test.getBlockEntity(CORE);
        var menu = (MenuArcaneWorkbench) workbench.createMenu(1, actor.getInventory(), actor);
        var grid = workbench.getInventory();
        var aura = Objects.requireNonNull(AuraManager.chunkAt(level, test.absolutePos(CORE)));
        float oldVis = aura.getVis();
        try {
            aura.setVis(400);
            grid.setItem(2, new ItemStack(TTItems.WAND_CAP_GOLD.get()));
            grid.setItem(4, new ItemStack(Items.STICK));
            grid.setItem(6, new ItemStack(TTItems.WAND_CAP_GOLD.get()));
            test.assertTrue(menu.getSlot(0).getItem().isEmpty(), "The original rod still matched the edited recipe");
            grid.setItem(4, new ItemStack(Items.BLAZE_ROD));
            var preview = ArcaneCraftingTransaction.preview(workbench.craftingContext(actor), actor,
                    grid.asArcaneCraftInput().withPlayer(actor));
            test.assertTrue(preview.successful() && preview.cost() != null && preview.cost().auraVis() > 0,
                    "The edited generated recipe did not offer its original vis cost");
            menu.clicked(0, 0, ClickType.PICKUP, actor);
            var result = menu.getCarried();
            var parts = WandVisHelper.getParts(result);
            test.assertTrue(result.is(TTItems.WAND.get()) && parts.cap() == TTWandParts.CAP_GOLD.get()
                            && parts.rod() == TTWandParts.ROD_WOOD.get() && !parts.sceptre(),
                    "The generated wand lost its original configured parts");
            test.assertTrue(grid.getItem(2).isEmpty() && grid.getItem(4).isEmpty() && grid.getItem(6).isEmpty()
                            && aura.getVis() == 400 - preview.cost().auraVis(),
                    "The edited generated recipe did not spend its inputs and previewed vis once");
        } finally {
            aura.setVis(oldVis);
            menu.removed(actor);
            StageGameTests.remove(test, actor);
        }
        test.succeed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_assembly", timeoutTicks = 160)
    public static void authoredBlueprintRotatesAndChecksItsTag(GameTestHelper test) {
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var pos = test.absolutePos(CORE);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var dust = actor.getMainHandItem();
        test.setBlock(CORE, Blocks.AMETHYST_BLOCK);
        test.setBlock(CORE.south(), Blocks.EMERALD_BLOCK);
        dust.getItem().useOn(new UseOnContext(actor, InteractionHand.MAIN_HAND, hit));
        test.assertTrue(dust.getCount() == 2, "The locked rotated blueprint consumed Salis Mundus");
        stages.addStage(BLUEPRINT_STAGE);
        dust.getItem().useOn(new UseOnContext(actor, InteractionHand.MAIN_HAND, hit));
        test.assertTrue(dust.getCount() == 1, "The rotated blueprint did not spend exactly one Salis Mundus");
        test.startSequence().thenWaitUntil(() -> {
            test.assertBlockPresent(Blocks.BARREL, CORE);
            test.assertBlockPresent(Blocks.GLASS, CORE.south());
        }).thenExecute(() -> {
            test.assertTrue(test.getBlockState(CORE).getValue(BlockStateProperties.FACING) == Direction.SOUTH,
                    "The authored barrel did not follow the blueprint's rotation");
            StageGameTests.remove(test, actor);
        }).thenSucceed();
    }
}
