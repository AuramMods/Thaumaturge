package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSeals;
import java.util.List;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class GolemStageGameTests {
    private static final BlockPos PRESS = new BlockPos(2, 2, 2);

    private GolemStageGameTests() {}

    @GameTest(template = "golem_room", batch = "crafttweaker_golem", timeoutTicks = 220)
    public static void stagedGolemUsesAuthoredComponentsAndSavedMachina(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var design = GolemProperties.createDefault().withMaterial(TTGolemParts.IRON.get());
        List.of(design.material().research(), design.head().research(), design.arms().research(),
                design.legs().research(), design.addon().research())
                .forEach(gates -> gates.forEach(id -> ResearchManager.complete(actor, id)));
        var bill = design.assemblyBill();
        test.assertTrue(bill.components().stream().anyMatch(stack -> stack.is(Items.COPPER_INGOT) && stack.getCount() == 3)
                        && bill.machina() == bill.components().stream().mapToInt(ItemStack::getCount).sum()
                                + design.traits().size() * 2 + 2,
                "The tagged material rule did not replace its component share and adjust Machina");
        for (ItemStack component : bill.components()) {
            test.assertTrue(actor.getInventory().add(component.copy()), "Could not stock the assembly components");
        }
        test.setBlock(PRESS, TTBlocks.GOLEM_BUILDER.get());
        var press = (BlockEntityGolemBuilder) test.getBlockEntity(PRESS);
        test.assertTrue(!press.beginAssembly(design, actor) && actor.getInventory().countItem(Items.COPPER_INGOT) == 3,
                "The material tag did not gate the direct assembly API before payment");
        stages.addStage("assemble_iron_golem");
        test.assertTrue(press.beginAssembly(design, actor) && press.totalCost() == bill.machina(),
                "The press did not accept the authored assembly bill");
        test.assertTrue(actor.getInventory().countItem(Items.COPPER_INGOT) == 0, "The press did not spend three copper");

        var machina = Objects.requireNonNull(Aspects.resolve(level, TTAspects.MACHINA));
        test.assertTrue(press.insert(machina, 1, Direction.NORTH, false) == 1, "The press did not buffer one Machina");
        test.setBlock(PRESS.below(), TTBlocks.JAR_NORMAL.get());
        var jar = (BlockEntityJar) test.getBlockEntity(PRESS.below());
        jar.setStorageContents(TTAspects.MACHINA, bill.machina() + 2);
        stages.removeStage("assemble_iron_golem");
        press.load(press.saveWithFullMetadata());

        test.startSequence().thenIdle(12).thenExecute(() -> {
            test.assertTrue(press.cost() == bill.machina() && jar.amount() == bill.machina() + 2
                            && press.outputHandler().getStackInSlot(0).isEmpty()
                            && press.insert(machina, 1, Direction.NORTH, true) == 0,
                    "The saved, revoked assembly advanced or accepted essentia");
            stages.addStage("assemble_iron_golem");
        }).thenWaitUntil(() -> test.assertTrue(press.outputHandler().getStackInSlot(0).is(TTItems.GOLEM_PLACER.get()),
                "Waiting for the press to deliver the staged build")).thenExecute(() -> {
            var placer = press.outputHandler().extractItem(0, 1, false);
            test.assertTrue(design.equals(ItemData.get(placer, TTDataComponents.GOLEM_PROPERTIES.get()))
                            && jar.amount() == 3, "Assembly changed the resulting parts or the saved Machina payment");
            BlockPos ground = PRESS.east(3).below();
            test.setBlock(ground, Blocks.SMOOTH_STONE);
            BlockPos absolute = test.absolutePos(ground);
            actor.setItemInHand(InteractionHand.MAIN_HAND, placer);
            var hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
            var use = new UseOnContext(actor, InteractionHand.MAIN_HAND, hit);
            test.assertTrue(!placer.useOn(use).consumesAction() && placer.getCount() == 1,
                    "Deployment consumed a restricted golem");
            stages.addStage("deploy_iron_golem");
            test.assertTrue(placer.useOn(use).consumesAction() && placer.isEmpty(), "The eligible actor could not deploy");
            var golems = test.getEntities(TTEntities.THAUMATURGE_GOLEM.get(), ground.above(), 1.5);
            test.assertTrue(golems.size() == 1 && actor.getUUID().equals(golems.get(0).getOwnerUUID()),
                    "The deployment did not produce one owned golem");
            StageGameTests.remove(test, actor);
        }).thenSucceed();
    }

    @GameTest(template = "golem_room", batch = "crafttweaker_golem_seal")
    public static void sealAnchorGatesPlacementEditingAndTaskAccess(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        test.setBlock(PRESS, Blocks.AMETHYST_BLOCK);
        var position = new SealPos(test.absolutePos(PRESS), Direction.UP);
        var type = TTSeals.GUARD_ADVANCED;
        test.assertTrue(!SealHandler.place(level, position, type.getId(), type.get(), actor),
                "The seal anchor did not gate the server placement API");
        stages.addStage("guard_advanced_seal");
        test.assertTrue(SealHandler.place(level, position, type.getId(), type.get(), actor),
                "The eligible actor could not place the seal");
        var seal = Objects.requireNonNull(SealHandler.getSealEntity(level, position));
        var golem = (EntityThaumaturgeGolem) TTEntities.THAUMATURGE_GOLEM.get().create(level);
        Objects.requireNonNull(golem).applyBuild(GolemProperties.createDefault()
                .withHead(TTGolemParts.HEAD_SMART.get()).withArms(TTGolemParts.ARMS_CLAWS.get()));
        test.assertTrue(SealAccess.allows(seal, golem) && SealAccess.mayEdit(actor, seal),
                "The stage interfered with an eligible worker or editor");
        stages.removeStage("guard_advanced_seal");
        test.assertTrue(!SealAccess.allows(seal, golem) && !SealAccess.mayEdit(actor, seal),
                "Revocation left an existing seal task or edit authorized");
        stages.addStage("guard_advanced_seal");
        test.assertTrue(SealAccess.allows(seal, golem) && SealHandler.getSealEntity(level, position) == seal,
                "Restoring the stage did not retain the native seal");
        StageGameTests.remove(test, actor);
        test.succeed();
    }
}
