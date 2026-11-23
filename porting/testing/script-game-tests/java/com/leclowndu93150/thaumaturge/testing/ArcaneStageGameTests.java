package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class ArcaneStageGameTests {
    private ArcaneStageGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_tagged_workbench")
    public static void shapedAndShapelessCraftsShareTaggedPrimalPayment(GameTestHelper test) {
        var position = new BlockPos(2, 3, 2);
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var aqua = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AQUA));
        var terra = Objects.requireNonNull(Aspects.resolve(level, TTAspects.TERRA));
        var motus = Objects.requireNonNull(Aspects.resolve(level, TTAspects.MOTUS));
        test.setBlock(position, TTBlocks.ARCANE_WORKBENCH.get());
        var workbench = (BlockEntityArcaneWorkbench) test.getBlockEntity(position);
        var inventory = workbench.getInventory();
        var menu = (MenuArcaneWorkbench) workbench.createMenu(1, actor.getInventory(), actor);
        var context = workbench.craftingContext(actor);
        AuraHelper.addVis(level, test.absolutePos(position), 20.0f);
        float initialVis = AuraHelper.getVis(level, test.absolutePos(position));
        inventory.setItem(0, new ItemStack(Items.AMETHYST_SHARD));
        inventory.setItem(9, EssentiaCrystalFactory.of(aqua, 2));
        inventory.setItem(10, EssentiaCrystalFactory.of(terra, 4));
        inventory.setItem(11, EssentiaCrystalFactory.of(motus, 1));

        var input = inventory.asArcaneCraftInput().withPlayer(actor);
        test.assertTrue(ArcaneCraftingTransaction.inspect(context, actor, input).failure() == Failure.STAGE_LOCKED,
                "The recipe should remain locked before the stage is granted");
        stages.addStage("tagged_workbench");
        test.assertTrue(ArcaneCraftingTransaction.preview(context, actor, input).failure() == Failure.PAYMENT_UNAVAILABLE,
                "A nonprimal member or double-counted crystal satisfied the payment");
        test.assertTrue(AuraHelper.getVis(level, test.absolutePos(position)) == initialVis,
                "An incomplete preview spent vis");

        inventory.setItem(9, EssentiaCrystalFactory.of(aqua, 3));
        test.assertTrue(menu.getSlot(0).getItem().is(Items.EMERALD), "The funded recipe did not show its result");
        stages.removeStage("tagged_workbench");
        test.assertTrue(menu.quickMoveStack(actor, 0).isEmpty() && inventory.getItem(9).getCount() == 3,
                "Taking a stale result bypassed the stage check");
        stages.addStage("tagged_workbench");
        menu.broadcastChanges();
        test.assertTrue(menu.quickMoveStack(actor, 0).is(Items.EMERALD)
                        && actor.getInventory().countItem(Items.EMERALD) == 1,
                "Native shift crafting did not award the scripted output exactly once");
        test.assertTrue(inventory.getItem(0).isEmpty() && inventory.getItem(9).isEmpty()
                        && inventory.getItem(10).getCount() == 1 && inventory.getItem(11).getCount() == 1,
                "The workbench spent the wrong concrete crystal allocation");
        test.assertTrue(AuraHelper.getVis(level, test.absolutePos(position)) < initialVis,
                "A successful tagged craft did not charge its aura vis");

        inventory.setItem(0, new ItemStack(Items.HEART_OF_THE_SEA));
        inventory.setItem(1, new ItemStack(Items.STICK));
        inventory.setItem(9, EssentiaCrystalFactory.of(aqua, 1));
        test.assertTrue(menu.quickMoveStack(actor, 0).is(Items.LAPIS_LAZULI)
                        && inventory.getItem(9).isEmpty(),
                "The shaped recipe did not spend its one matching crystal");
        menu.removed(actor);
        StageGameTests.remove(test, actor);
        test.succeed();
    }
}
