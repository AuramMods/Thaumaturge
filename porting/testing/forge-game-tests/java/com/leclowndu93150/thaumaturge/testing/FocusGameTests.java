package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.MenuFocalManipulator;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.network.ServerboundFocusChangePayload;
import com.leclowndu93150.thaumaturge.network.ServerboundSpellDraftPayload;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class FocusGameTests {
    private static final BlockPos TABLE = new BlockPos(1, 2, 2);
    private static final BlockPos CASTER = new BlockPos(4, 2, 1);
    private static final BlockPos TARGET = new BlockPos(4, 2, 6);
    private static final String FOCUS_NAME = "Ember";
    private static final int SPARE_CRYSTALS = 2;
    private static final int SPARE_LEVELS = 3;

    private FocusGameTests() {}

    @GameTest(template = "golem_room", timeoutTicks = 300)
    public static void inscribedFocusResumesAndCastsAProjectile(GameTestHelper test) {
        SealAutomationGameTests.prepareRoom(test);
        var player = ResearchGameTests.survivalPlayer(test);
        var draft = Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN)
                .then(SpellNode.of(TTSpellParts.PROJECTILE).then(SpellNode.of(TTSpellParts.FIRE))));
        test.setBlock(TABLE, TTBlocks.FOCAL_MANIPULATOR.get());
        placePlayer(test, player, TABLE.east());
        var aura = Objects.requireNonNull(AuraManager.chunkAt(test.getLevel(), test.absolutePos(TABLE)));
        short startingBase = aura.getBase();
        float startingVis = aura.getVis();
        aura.setBase((short) 0);
        aura.setVis(0);
        startInscription(test, player, draft);
        float fullCost = table(test).vis();

        test.startSequence().thenIdle(22).thenExecute(() -> {
            aura.setBase(startingBase);
            test.assertTrue(table(test).vis() == fullCost && table(test).progress() == 0
                            && Spells.spellOf(table(test).focusStack()) == null,
                    "The inscription advanced while the local aura was empty");
            aura.setVis(fullCost + 20);
        }).thenWaitUntil(() -> {
            float remaining = table(test).vis();
            test.assertTrue(remaining > 0 && remaining < fullCost, "Waiting for a real inscription work pulse");
        }).thenExecute(() -> reloadTable(test, draft)).thenWaitUntil(() -> {
            ItemStack focus = table(test).focusStack();
            test.assertTrue(!table(test).inscribing() && draft.equals(Spells.spellOf(focus)),
                    "Waiting for the restored table to inscribe the focus");
        }).thenExecute(() -> {
            aura.setVis(startingVis);
            ItemStack focus = takeFocus(test, player, draft);
            castAndCheckImpact(test, player, focus, draft);
        });
    }

    @GameTest(template = "golem_room")
    public static void frostBeamHonorsForgePlacementVeto(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var level = test.getLevel();
        var wall = TARGET.above();
        var allowed = wall.north().west();
        var protectedWater = wall.north().east();
        test.setBlock(wall, Blocks.STONE);
        for (var source : Set.of(allowed, protectedWater)) {
            test.setBlock(source.below(), Blocks.STONE);
            test.setBlock(source, Blocks.WATER);
        }
        placePlayer(test, player, CASTER);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(test.absolutePos(wall)));
        ResearchManager.complete(player, TTIds.rl("unlock_auromancy"));
        ResearchManager.complete(player, TTIds.rl("base_auromancy"));
        ResearchManager.complete(player, TTIds.rl("focus_beam"));
        ResearchManager.complete(player, TTIds.rl("focus_elemental"));
        var design = Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN)
                .then(SpellNode.of(TTSpellParts.BEAM).then(SpellNode.of(TTSpellParts.FROST))));
        var focus = new ItemStack(TTItems.FOCUS_1.get());
        Spells.setSpell(focus, design);
        var wand = new ItemStack(TTItems.WAND.get());
        TTItems.WAND.get().socketFocus(wand, focus);
        int capacity = WandVisHelper.getMaxVis(wand);
        for (var primal : TTAspects.PRIMALS) {
            WandVisHelper.storeVis(wand, primal, capacity);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);

        var allowedPos = test.absolutePos(allowed);
        var protectedPos = test.absolutePos(protectedWater);
        Set<BlockPos> placementAttempts = new HashSet<>();
        Consumer<BlockEvent.EntityPlaceEvent> veto = event -> {
            if (event.getEntity() == player) {
                placementAttempts.add(event.getPos().immutable());
                if (event.getPos().equals(protectedPos)) {
                    event.setCanceled(true);
                }
            }
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            test.assertTrue(wand.use(level, player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS,
                    "The loaded Frost focus did not cast");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
        test.assertTrue(placementAttempts.equals(Set.of(allowedPos, protectedPos)),
                "Forge did not receive both water placement attempts from the caster");
        test.assertBlockPresent(Blocks.FROSTED_ICE, allowed);
        test.assertBlockPresent(Blocks.WATER, protectedWater);
        test.assertTrue(level.getBlockTicks().hasScheduledTick(allowedPos, Blocks.FROSTED_ICE)
                        && !level.getBlockTicks().hasScheduledTick(protectedPos, Blocks.FROSTED_ICE),
                "Only the permitted ice should have a thaw tick");
        test.succeed();
    }

    private static void startInscription(GameTestHelper test, FakePlayer player, Spell draft) {
        var table = table(test);
        var level = test.getLevel();
        var pos = table.getBlockPos();
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.EAST, pos, false);
        var state = table.getBlockState();
        state.use(level, player, InteractionHand.MAIN_HAND, hit);
        test.assertTrue(player.containerMenu == player.inventoryMenu, "An unresearched player opened the editor");
        ResearchManager.complete(player, TTIds.rl("unlock_auromancy"));
        ResearchManager.complete(player, TTIds.rl("base_auromancy"));
        state.use(level, player, InteractionHand.MAIN_HAND, hit);
        test.assertTrue(player.containerMenu instanceof MenuFocalManipulator, "The researched editor did not open");
        var menu = (MenuFocalManipulator) player.containerMenu;
        var blank = new ItemStack(TTItems.FOCUS_1.get());
        var tier = Spells.tierOf(blank).orElseThrow();
        menu.setCarried(blank);
        menu.clicked(0, 0, ClickType.PICKUP, player);
        test.assertTrue(menu.getCarried().isEmpty() && table.focusStack().is(TTItems.FOCUS_1.get()),
                "The native focus slot did not accept the blank focus");

        var context = new GameTestPayloadContext(player);
        ServerboundSpellDraftPayload.handle(new ServerboundSpellDraftPayload(pos.north(), FOCUS_NAME, draft), context);
        test.assertTrue(table.draft().isEmpty(), "A draft for a different block reached the open editor");
        ServerboundSpellDraftPayload.handle(new ServerboundSpellDraftPayload(pos, " " + FOCUS_NAME + " ", draft), context);
        test.assertTrue(table.draft().equals(draft) && table.name().equals(FOCUS_NAME),
                "The editor did not retain the submitted tree and trimmed name");

        var summary = Spells.analyze(draft, tier, level.registryAccess(), player);
        var crystals = FocusItems.aspects(summary, level.registryAccess());
        test.assertTrue(!summary.valid() && crystals.totalAmount() > 0 && summary.xp() > 0,
                "The projectile draft should require research, crystals and experience");
        player.giveExperienceLevels(summary.xp() + SPARE_LEVELS);
        int crystalSlot = 9;
        for (AspectInstance cost : crystals.entries()) {
            var stock = EssentiaCrystalFactory.of(cost.aspect(), cost.amount() + SPARE_CRYSTALS);
            player.getInventory().setItem(crystalSlot++, stock);
        }
        menu.clickMenuButton(player, MenuFocalManipulator.BUTTON_INSCRIBE);
        test.assertTrue(!table.inscribing() && player.experienceLevel == summary.xp() + SPARE_LEVELS
                        && player.getInventory().countItem(TTItems.ESSENTIA_CRYSTAL.get())
                        == crystals.totalAmount() + SPARE_CRYSTALS * crystals.entries().size(),
                "The locked projectile draft spent materials");

        ResearchManager.complete(player, TTIds.rl("focus_projectile"));
        menu.clickMenuButton(player, MenuFocalManipulator.BUTTON_INSCRIBE);
        menu.clickMenuButton(player, MenuFocalManipulator.BUTTON_INSCRIBE);
        test.assertTrue(table.inscribing() && table.vis() > 20 && player.experienceLevel == SPARE_LEVELS,
                "The unlocked job did not start with exactly one experience payment");
        crystalSlot = 9;
        for (AspectInstance cost : crystals.entries()) {
            var expected = EssentiaCrystalFactory.of(cost.aspect(), SPARE_CRYSTALS);
            test.assertTrue(ItemStack.matches(player.getInventory().getItem(crystalSlot++), expected),
                    "Starting the inscription spent the wrong number of crystals of " + cost.aspect());
        }
        player.closeContainer();
    }

    private static void reloadTable(GameTestHelper test, Spell draft) {
        var original = table(test);
        var level = test.getLevel();
        var pos = original.getBlockPos();
        var state = original.getBlockState();
        var saved = original.saveWithFullMetadata();
        float remaining = original.vis();
        var paidCrystals = original.crystals().entries();
        level.removeBlockEntity(pos);
        var replacement = new BlockEntityFocalManipulator(pos, state);
        level.setBlockEntity(replacement);
        replacement.load(saved);
        test.assertTrue(table(test) == replacement && original.isRemoved() && replacement.vis() == remaining
                        && replacement.inscribing() && replacement.crystals().entries().equals(paidCrystals)
                        && replacement.draft().equals(draft) && replacement.name().equals(FOCUS_NAME),
                "Native block-entity reload lost the paid inscription or its draft");
    }

    private static ItemStack takeFocus(GameTestHelper test, FakePlayer player, Spell draft) {
        var menu = (MenuFocalManipulator) table(test).createMenu(2, player.getInventory(), player);
        menu.clicked(0, 0, ClickType.PICKUP, player);
        var focus = ItemStack.of(menu.getCarried().save(new CompoundTag()));
        menu.setCarried(ItemStack.EMPTY);
        menu.removed(player);
        test.assertTrue(focus.is(TTItems.FOCUS_1.get()) && focus.getCount() == 1 && draft.equals(Spells.spellOf(focus))
                        && focus.getHoverName().getString().equals(FOCUS_NAME)
                        && table(test).focusStack().isEmpty(),
                "Taking and saving the focus lost its spell, its name, or its single-item transfer");
        return focus;
    }

    private static void castAndCheckImpact(GameTestHelper test, FakePlayer player, ItemStack focus, Spell draft) {
        var level = test.getLevel();
        var wand = new ItemStack(TTItems.WAND.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        player.getInventory().setItem(1, focus);
        var context = new GameTestPayloadContext(player);
        ServerboundFocusChangePayload.handle(new ServerboundFocusChangePayload(FocusItems.sortKey(focus)), context);
        test.assertTrue(player.getInventory().getItem(1).isEmpty()
                        && draft.equals(Spells.spellOf(TTItems.WAND.get().socketedFocus(wand))),
                "Selecting the inscribed focus did not move it from inventory into the wand");

        placePlayer(test, player, CASTER);
        var target = test.spawnWithNoFreeWill(EntityType.PIG, TARGET);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getBoundingBox().getCenter());
        var summary = Spells.analyze(draft, Spells.tierOf(focus).orElseThrow(), level.registryAccess(), player);
        var costs = FocusItems.visSplit(summary, 1, level.registryAccess());
        var rejected = wand.use(level, player, InteractionHand.MAIN_HAND);
        test.assertTrue(rejected.getResult() == InteractionResult.FAIL && !CasterManager.isOnCooldown(player),
                "Casting with an empty wand did not fail cleanly");
        test.assertEntityNotPresent(TTEntities.FOCUS_PROJECTILE.get());

        int capacity = WandVisHelper.getMaxVis(wand);
        for (var primal : TTAspects.PRIMALS) {
            WandVisHelper.storeVis(wand, primal, capacity);
        }
        level.addNewPlayer(player);
        try {
            test.assertTrue(wand.use(level, player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.SUCCESS,
                    "The charged wand refused an affordable instant spell");
            var projectiles = test.getEntities(TTEntities.FOCUS_PROJECTILE.get(), CASTER, 2.5);
            test.assertTrue(projectiles.size() == 1 && projectiles.get(0).getOwner() == player,
                    "The native wand cast did not spawn one owned projectile");
            for (var primal : TTAspects.PRIMALS) {
                int owed = (int) (costs.getOrDefault(primal, 0)
                        * WandVisHelper.getConsumptionModifier(wand, player, primal, false));
                test.assertTrue(WandVisHelper.getVis(wand, primal) == capacity - owed,
                        "The spell charged the wrong amount of vis for " + primal.location());
            }
            var afterCast = WandVisHelper.getAllVis(wand);
            test.assertTrue(CasterManager.isOnCooldown(player)
                            && wand.use(level, player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.PASS
                            && WandVisHelper.getAllVis(wand).equals(afterCast),
                    "An immediate repeat cast bypassed cooldown or spent vis again");
            var projectile = projectiles.get(0);
            test.runAfterDelay(18, () -> {
                try {
                    test.assertTrue(projectile.isRemoved() && target.getHealth() < target.getMaxHealth()
                                    && target.isOnFire() && target.getLastHurtByMob() == player,
                            "The ticking projectile did not apply attributed fire damage at impact");
                    test.succeed();
                } finally {
                    level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
                }
            });
        } catch (RuntimeException | Error failure) {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            throw failure;
        }
    }

    private static BlockEntityFocalManipulator table(GameTestHelper test) {
        return (BlockEntityFocalManipulator) test.getBlockEntity(TABLE);
    }

    private static void placePlayer(GameTestHelper test, FakePlayer player, BlockPos at) {
        var pos = Vec3.atBottomCenterOf(test.absolutePos(at));
        player.moveTo(pos.x, pos.y, pos.z);
    }
}
