package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.spell.casting.SpellCasting;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.MenuFocalManipulator;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.network.ServerboundSpellDraftPayload;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class FocusStageGameTests {
    private static final BlockPos TABLE = new BlockPos(2, 2, 2);

    private FocusStageGameTests() {}

    @GameTest(template = "golem_room", batch = "crafttweaker_focus", timeoutTicks = 140)
    public static void focusChargesScriptedCostsAndStagesBothEntryPoints(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var pos = test.absolutePos(TABLE);
        ResearchManager.complete(actor, TTIds.rl("unlock_auromancy"));
        ResearchManager.complete(actor, TTIds.rl("base_auromancy"));
        ResearchManager.complete(actor, TTIds.rl("focus_projectile"));
        var motus = Objects.requireNonNull(Aspects.resolve(level, TTAspects.MOTUS));
        AspectPools.grant(actor, motus, 1);
        var projectile = ResourceKey.create(SpellPart.REGISTRY_KEY, new ResourceLocation("crafttweaker", "tagged_projectile"));
        var spell = Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN)
                .then(SpellNode.of(projectile).then(SpellNode.of(TTSpellParts.FIRE))));
        var focus = new ItemStack(TTItems.FOCUS_3.get());
        var tier = Spells.tierOf(focus).orElseThrow();
        var base = Spells.analyze(spell, tier, level.registryAccess(), actor);
        test.assertTrue(base.valid() && base.xp() > 0 && tier.complexity() == 40,
                "The data-driven spell part or scripted focus tier did not load");

        test.setBlock(TABLE, TTBlocks.FOCAL_MANIPULATOR.get());
        var table = (BlockEntityFocalManipulator) test.getBlockEntity(TABLE);
        var menu = (MenuFocalManipulator) table.createMenu(1, actor.getInventory(), actor);
        actor.containerMenu = menu;
        table.items().setStackInSlot(0, focus);
        ServerboundSpellDraftPayload.handle(new ServerboundSpellDraftPayload(pos, "Test Focus", spell),
                new GameTestPayloadContext(actor));
        test.assertTrue(table.draft().equals(spell), "The native draft packet did not populate the table");
        actor.giveExperienceLevels(base.xp() * 2 + 4);
        int stock = 0;
        int cost = 0;
        int slot = 9;
        for (var entry : base.crystals().entrySet()) {
            var aspect = Objects.requireNonNull(Aspects.resolve(level, entry.getKey()));
            int scaled = Math.round(entry.getValue() * 0.5f);
            actor.getInventory().setItem(slot++, EssentiaCrystalFactory.of(aspect, scaled + 2));
            stock += scaled + 2;
            cost += scaled;
        }
        AuraHelper.addVis(level, pos, 120.0f);
        menu.clickMenuButton(actor, MenuFocalManipulator.BUTTON_INSCRIBE);
        test.assertTrue(!table.inscribing() && actor.experienceLevel == base.xp() * 2 + 4
                        && actor.getInventory().countItem(TTItems.ESSENTIA_CRYSTAL.get()) == stock,
                "A stage-denied inscription consumed experience or crystals");
        stages.addStage("inscribe_test_spell");
        menu.clickMenuButton(actor, MenuFocalManipulator.BUTTON_INSCRIBE);
        float vis = (base.complexity() * 10 + tier.complexity() / 5.0f) * 0.5f;
        test.assertTrue(table.inscribing() && table.vis() == vis && actor.experienceLevel == 4
                        && actor.getInventory().countItem(TTItems.ESSENTIA_CRYSTAL.get()) == stock - cost,
                "The inscription did not charge the scripted costs");
        stages.removeStage("inscribe_test_spell");
        table.load(table.saveWithFullMetadata());

        test.startSequence().thenIdle(12).thenExecute(() -> {
            test.assertTrue(table.vis() == vis && Spells.spellOf(table.focusStack()) == null,
                    "The saved inscription spent aura or completed after revocation");
            stages.addStage("inscribe_test_spell");
        }).thenWaitUntil(() -> test.assertTrue(spell.equals(Spells.spellOf(table.focusStack())),
                "Waiting for the authorized focus to finish")).thenExecute(() -> {
            var wand = new ItemStack(TTItems.WAND.get());
            actor.setItemInHand(InteractionHand.MAIN_HAND, wand);
            for (var primal : TTAspects.PRIMALS) WandVisHelper.storeVis(wand, primal, WandVisHelper.getMaxVis(wand));
            var before = WandVisHelper.getAllVis(wand);
            test.assertTrue(SpellCasting.use(level, actor, InteractionHand.MAIN_HAND, wand, table.focusStack())
                            == InteractionResult.FAIL && before.equals(WandVisHelper.getAllVis(wand)),
                    "A blocked wand cast spent vis");
            Spells.cast(actor, spell, 1.0f);
            test.assertEntityNotPresent(TTEntities.FOCUS_PROJECTILE.get());
            stages.addStage("cast_test_spell");
            test.assertTrue(SpellCasting.use(level, actor, InteractionHand.MAIN_HAND, wand, table.focusStack())
                            == InteractionResult.SUCCESS && !before.equals(WandVisHelper.getAllVis(wand)),
                    "The staged wand cast did not use vis");
            test.assertEntityPresent(TTEntities.FOCUS_PROJECTILE.get());
            actor.containerMenu = actor.inventoryMenu;
            StageGameTests.remove(test, actor);
        }).thenSucceed();
    }
}
