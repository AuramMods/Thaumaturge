package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedBlock;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class ResearchGameTests {
    private static final String ROOM = "essentia_room";
    private static final BlockPos CENTER = new BlockPos(2, 1, 2);
    private static final BlockPos BOOKCASE = CENTER.east();
    private static final BlockPos BED = CENTER.west();
    private static final BlockPos CRUCIBLE = CENTER.south();
    private static final InteractionHand DUST_HAND = InteractionHand.OFF_HAND;

    private ResearchGameTests() {}

    @GameTest(template = ROOM)
    public static void arcaneWorkbenchUsesNativeGridForOrdinaryRecipes(GameTestHelper test) {
        var level = test.getLevel();
        var player = survivalPlayer(test);
        var manager = level.getRecipeManager();
        var recipes = List.copyOf(manager.getRecipes());
        var access = RecipeAccess.of(manager);
        var definitions = new HashMap<ResourceLocation, JsonElement>();
        for (var recipe : recipes) {
            access.sourceDefinition(recipe).ifPresent(definition -> definitions.put(recipe.getId(), definition));
        }
        test.setBlock(CENTER, TTBlocks.ARCANE_WORKBENCH.get());
        var pos = test.absolutePos(CENTER);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);

        try {
            manager.replaceRecipes(List.of(fixedSlotRecipe()));
            var result = player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
            test.assertTrue(result.consumesAction() && player.containerMenu instanceof MenuArcaneWorkbench,
                    "The workbench should open even if a recipe reads fixed slots on an empty grid");
            var menu = (MenuArcaneWorkbench) player.containerMenu;
            menu.getSlot(1).set(new ItemStack(Items.PAPER));
            menu.getSlot(5).set(new ItemStack(Items.FEATHER));
            test.assertTrue(menu.getSlot(0).getItem().is(Items.BOOK), "The recipe should see the original grid positions");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            test.assertTrue(menu.getCarried().is(Items.BOOK) && menu.getSlot(1).getItem().isEmpty()
                            && menu.getSlot(5).getItem().is(Items.STICK),
                    "Crafting should consume the ingredients and return the remainder to its original slot");
        } finally {
            player.closeContainer();
            manager.replaceRecipes(recipes);
            access.recipesApplied(definitions);
        }
        test.succeed();
    }

    private static CustomRecipe fixedSlotRecipe() {
        return new CustomRecipe(TTIds.rl("test_fixed_slot"), CraftingBookCategory.MISC) {
            @Override
            public boolean matches(CraftingContainer grid, Level level) {
                // Blood Magic reads from the grid list, and Quark reads by slot index.
                return grid.getItems().get(0).is(Items.PAPER) && grid.getItem(4).is(Items.FEATHER);
            }

            @Override
            public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
                return new ItemStack(Items.BOOK);
            }

            @Override
            public NonNullList<ItemStack> getRemainingItems(CraftingContainer grid) {
                var remainders = NonNullList.withSize(grid.getContainerSize(), ItemStack.EMPTY);
                remainders.set(4, new ItemStack(Items.STICK));
                return remainders;
            }

            @Override
            public boolean canCraftInDimensions(int width, int height) {
                return width == 3 && height == 3;
            }

            @Override
            public RecipeSerializer<?> getSerializer() {
                return RecipeSerializer.ARMOR_DYE;
            }
        };
    }

    @GameTest(template = ROOM)
    public static void arcaneWorkbenchOpensWithoutDreamResearch(GameTestHelper test) {
        var player = survivalPlayer(test);
        test.assertFalse(PlayerResearchAccess.of(player).hasCompleted(TTIds.rl("gotdream")),
                "The player should start without the dream discovery");
        test.setBlock(CENTER, TTBlocks.ARCANE_WORKBENCH.get());
        var pos = test.absolutePos(CENTER);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);

        try {
            var result = player.gameMode.useItemOn(player, test.getLevel(), player.getMainHandItem(),
                    InteractionHand.MAIN_HAND, hit);
            test.assertTrue(result.consumesAction() && player.containerMenu instanceof MenuArcaneWorkbench,
                    "Right-clicking the Arcane Workbench should open its menu before the dream discovery");
            var menu = (MenuArcaneWorkbench) player.containerMenu;
            test.assertTrue(menu.stillValid(player), "The workbench menu should be bound to the clicked block");
            menu.getSlot(1).set(new ItemStack(Items.OAK_LOG));
            var planks = menu.getSlot(0).getItem();
            test.assertTrue(planks.is(Items.OAK_PLANKS) && planks.getCount() == 4,
                    "The workbench should preview an ordinary recipe for the new player");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            test.assertTrue(ItemStack.matches(menu.getCarried(), new ItemStack(Items.OAK_PLANKS, 4))
                            && menu.getSlot(1).getItem().isEmpty(),
                    "Taking the result should craft four planks and consume the log");
        } finally {
            player.closeContainer();
        }
        test.succeed();
    }

    @GameTest(template = ROOM)
    public static void crystalPickupAndWakeAwardJournalOnce(GameTestHelper test) {
        var player = survivalPlayer(test);
        var research = PlayerResearchAccess.of(player);
        test.assertFalse(research.knows(TTIds.rl("gotcrystals")), "New player already knew the crystal discovery");
        pickUpCrystal(test, player);
        test.assertTrue(research.hasCompleted(TTIds.rl("gotcrystals")), "Crystal pickup did not complete the discovery");
        test.assertFalse(research.knows(TTIds.rl("gotdream")), "Dream unlocked before waking");

        wakeInBed(test, player);
        test.assertTrue(research.hasCompleted(TTIds.rl("gotdream")), "Waking did not complete the dream research");
        ItemStack journal = player.getInventory().items.stream()
                .filter(stack -> stack.is(Items.WRITTEN_BOOK))
                .findFirst()
                .orElse(ItemStack.EMPTY);
        var contents = journal.getTag();
        test.assertTrue(contents != null && WrittenBookItem.makeSureTagIsValid(contents)
                        && player.getGameProfile().getName().equals(contents.getString(WrittenBookItem.TAG_AUTHOR))
                        && contents.getList(WrittenBookItem.TAG_PAGES, Tag.TAG_STRING).size() == 3,
                "Dream journal did not contain a valid title, author and three pages");

        wakeInBed(test, player);
        test.assertTrue(player.getInventory().countItem(Items.WRITTEN_BOOK) == 1, "Waking again duplicated the journal");
        test.succeed();
    }

    @GameTest(template = ROOM)
    public static void starterWandCraftRecordsResearchAndConsumesParts(GameTestHelper test) {
        var player = survivalPlayer(test);
        var research = PlayerResearchAccess.of(player);
        var craftedKey = ResearchManager.craftedKey(TTIds.rl("wand"));
        test.setBlock(CENTER, TTBlocks.ARCANE_WORKBENCH.get());
        var workbench = (BlockEntityArcaneWorkbench) test.getBlockEntity(CENTER);
        var menu = (MenuArcaneWorkbench) workbench.createMenu(1, player.getInventory(), player);
        try {
            var grid = workbench.getInventory();
            grid.setItem(2, new ItemStack(TTItems.WAND_CAP_IRON.get()));
            grid.setItem(4, new ItemStack(Items.STICK));
            grid.setItem(6, new ItemStack(TTItems.WAND_CAP_IRON.get()));
            test.assertTrue(menu.getSlot(0).getItem().is(TTItems.WAND.get()), "Workbench did not find the starter recipe");
            test.assertFalse(research.knows(craftedKey), "Preview awarded crafting research prematurely");

            menu.clicked(0, 0, ClickType.PICKUP, player);
            var wand = menu.getCarried();
            test.assertTrue(wand.is(TTItems.WAND.get()) && wand.getCount() == 1, "Result slot did not yield one wand");
            test.assertTrue(ItemData.get(wand, TTDataComponents.WAND_PARTS.get()) != null, "Crafted wand lost its parts tag");
            var parts = WandVisHelper.getParts(wand);
            test.assertTrue(parts.cap() == TTWandParts.CAP_IRON.get() && parts.rod() == TTWandParts.ROD_WOOD.get()
                            && !parts.sceptre(),
                    "Crafted wand did not retain the registered iron cap and wood rod");
            test.assertTrue(grid.getItem(2).isEmpty() && grid.getItem(4).isEmpty() && grid.getItem(6).isEmpty(),
                    "Result pickup left a crafting ingredient behind");
            test.assertTrue(menu.getSlot(0).getItem().isEmpty(), "Workbench offered a second result after consumption");
            test.assertTrue(research.knows(craftedKey), "Committed craft did not record research");
        } finally {
            menu.removed(player);
        }
        test.succeed();
    }

    @GameTest(template = ROOM, timeoutTicks = 420)
    public static void openingProgressionThroughNitor(GameTestHelper test) {
        var player = survivalPlayer(test);
        var research = PlayerResearchAccess.of(player);
        var firstSteps = TTIds.rl("first_steps");
        var dust = craftSalisMundus(test, player);
        player.setItemInHand(DUST_HAND, dust);
        test.setBlock(BOOKCASE, Blocks.BOOKSHELF);

        test.assertFalse(ResearchManager.unlockRequested(player, firstSteps), "First Steps unlocked without the book");
        test.assertTrue(!useDust(test, player, BOOKCASE).consumesAction() && dust.getCount() == 3,
                "The bookshelf recipe consumed dust before the dream");
        pickUpCrystal(test, player);
        wakeInBed(test, player);
        test.assertTrue(useDust(test, player, BOOKCASE).consumesAction() && dust.getCount() == 2,
                "The bookshelf recipe did not consume one dust after the dream");

        test.startSequence().thenWaitUntil(() -> {
            test.assertTrue(test.getBlockState(BOOKCASE).isAir(), "Waiting for the bookshelf transformation");
            var books = test.getEntities(TTEntities.SPECIAL_ITEM.get(), BOOKCASE, 1.5);
            test.assertTrue(books.size() == 1 && books.get(0).getItem().is(TTItems.THAUMONOMICON.get()),
                    "The bookshelf did not yield one Thaumonomicon");
            var book = books.get(0);
            book.setNoPickUpDelay();
            book.playerTouch(player);
            test.assertTrue(research.hasCompleted(TTIds.rl("gotthaumonomicon")),
                    "Picking up the Thaumonomicon did not unlock its research");
        }).thenExecute(() -> {
            test.assertTrue(ResearchManager.unlockRequested(player, firstSteps), "The Thaumonomicon did not unlock First Steps");
            test.assertFalse(ResearchManager.advanceStage(player, firstSteps), "First Steps advanced without a workbench");
            test.assertTrue(useDust(test, player, CENTER).consumesAction() && dust.getCount() == 1,
                    "The crafting table recipe did not consume one dust");
        }).thenWaitUntil(() -> {
            test.assertTrue(test.getBlockState(CENTER).is(TTBlocks.ARCANE_WORKBENCH.get()),
                    "Waiting for the crafting table transformation");
        }).thenExecute(() -> {
            var thaumometer = craftThaumometer(test, player);
            finishFirstSteps(test, player, thaumometer);
            player.setItemInHand(DUST_HAND, dust);
            test.assertTrue(player.getInventory().add(thaumometer), "The player could not store the Thaumometer");
            makeCrucible(test, player, dust);
        }).thenWaitUntil(() -> {
            test.assertTrue(test.getBlockState(CRUCIBLE).is(TTBlocks.CRUCIBLE.get()),
                    "Waiting for the cauldron transformation");
        }).thenExecute(() -> fillCrucible(test, player)).thenWaitUntil(() -> {
            test.assertTrue(crucible(test).getHeat() > 150, "Waiting for the crucible to boil over the magma block");
        }).thenExecute(() -> craftNitor(test, player)).thenWaitUntil(() -> {
            var drops = test.getEntities(TTEntities.SPECIAL_ITEM.get(), CRUCIBLE, 1.5);
            test.assertTrue(drops.size() == 1 && drops.get(0).getItem().is(TTItems.NITORS.get(DyeColor.YELLOW).get())
                            && drops.get(0).getItem().getCount() == 1,
                    "The crucible did not eject exactly one yellow Nitor");
        }).thenSucceed();
    }

    private static ItemStack craftSalisMundus(GameTestHelper test, FakePlayer player) {
        test.setBlock(CENTER, Blocks.CRAFTING_TABLE);
        var inventory = player.getInventory();
        var menu = new CraftingMenu(1, inventory,
                ContainerLevelAccess.create(test.getLevel(), test.absolutePos(CENTER)));
        try {
            menu.getSlot(1).set(new ItemStack(Items.BOWL));
            menu.getSlot(2).set(new ItemStack(Items.FLINT));
            menu.getSlot(3).set(new ItemStack(Items.REDSTONE, 3));
            menu.getSlot(4).set(crystal(test, TTAspects.AER, 3));
            menu.getSlot(5).set(crystal(test, TTAspects.IGNIS, 3));
            menu.getSlot(6).set(crystal(test, TTAspects.AER, 3));
            test.assertTrue(menu.getSlot(0).getItem().isEmpty(), "Salis Mundus accepted duplicate aspects");
            menu.getSlot(6).set(crystal(test, TTAspects.AQUA, 3));
            test.assertTrue(menu.getSlot(0).getItem().is(TTItems.SALIS_MUNDUS.get()),
                    "Three different crystals did not produce a Salis Mundus preview");
            menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
            test.assertTrue(inventory.countItem(TTItems.SALIS_MUNDUS.get()) == 3, "Expected three crafted dusts");
            test.assertTrue(menu.getSlot(1).getItem().is(Items.BOWL) && menu.getSlot(2).getItem().is(Items.FLINT),
                    "Crafting did not return the bowl and flint");
            int slot = inventory.findSlotMatchingItem(new ItemStack(TTItems.SALIS_MUNDUS.get()));
            return inventory.removeItem(slot, 3);
        } finally {
            menu.removed(player);
        }
    }

    private static ItemStack craftThaumometer(GameTestHelper test, FakePlayer player) {
        var workbench = (BlockEntityArcaneWorkbench) test.getBlockEntity(CENTER);
        var grid = workbench.getInventory();
        var menu = (MenuArcaneWorkbench) workbench.createMenu(2, player.getInventory(), player);
        var aura = Objects.requireNonNull(AuraManager.chunkAt(test.getLevel(), workbench.getBlockPos()));
        float originalVis = aura.getVis();
        try {
            aura.setVis(100);
            for (int slot : new int[] {1, 3, 5, 7}) {
                grid.setItem(slot, new ItemStack(Items.GOLD_INGOT));
            }
            grid.setItem(4, new ItemStack(Items.GLASS_PANE));
            for (int index = 0; index < MenuArcaneWorkbench.PRIMAL_ORDER.size(); index++) {
                grid.setItem(MenuArcaneWorkbench.CRYSTAL_SLOT_START + index,
                        crystal(test, MenuArcaneWorkbench.PRIMAL_ORDER.get(index), 1));
            }
            var madeThaumometer = ResearchManager.craftedKey(TTIds.rl("thaumometer"));
            var research = PlayerResearchAccess.of(player);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            test.assertTrue(menu.getSlot(0).getItem().isEmpty() && menu.getCarried().isEmpty()
                            && aura.getVis() == 100 && !research.knows(madeThaumometer),
                    "The research-locked recipe previewed, consumed vis, or awarded credit");

            var firstSteps = TTIds.rl("first_steps");
            test.assertTrue(ResearchManager.advanceStage(player, firstSteps) && research.stageOf(firstSteps) == 1,
                    "Transforming the crafting table did not satisfy the First Steps requirement");
            aura.setVis(24);
            menu.slotsChanged(grid);
            test.assertTrue(menu.getSlot(0).getItem().isEmpty() && aura.getVis() == 24,
                    "The workbench offered a crystal-funded craft with less than 25 vis");
            AuraHelper.addVis(test.getLevel(), workbench.getBlockPos(), 1);
            menu.slotsChanged(grid);
            test.assertTrue(menu.getSlot(0).getItem().is(TTItems.THAUMOMETER.get()) && aura.getVis() == 25,
                    "The unlocked preview did not appear at 25 vis or charged the preview");

            menu.clicked(0, 0, ClickType.PICKUP, player);
            test.assertTrue(menu.getCarried().is(TTItems.THAUMOMETER.get()) && menu.getCarried().getCount() == 1,
                    "The unlocked recipe did not yield one Thaumometer");
            test.assertTrue(grid.isEmpty() && aura.getVis() == 0 && research.knows(madeThaumometer),
                    "The craft did not consume its ingredients, all six crystals and exactly 25 vis or record research");
            test.assertFalse(ResearchManager.advanceStage(player, firstSteps), "First Steps completed without observations");
            var thaumometer = menu.getCarried();
            menu.setCarried(ItemStack.EMPTY);
            return thaumometer;
        } finally {
            aura.setVis(originalVis);
            menu.removed(player);
        }
    }

    private static void finishFirstSteps(GameTestHelper test, FakePlayer player, ItemStack thaumometer) {
        test.setBlock(BOOKCASE, Blocks.CHEST);
        var chest = (ChestBlockEntity) test.getBlockEntity(BOOKCASE);
        // The scanner must retry advanced subjects after their components are discovered.
        Item[] subjects = {Items.GOLDEN_APPLE, Items.BOOK, Items.SOUL_TORCH, Items.BONE,
                Items.REDSTONE, Items.TORCH, Items.MUSHROOM_STEW, Items.GLISTERING_MELON_SLICE, Items.CLAY_BALL};
        for (int index = 0; index < subjects.length; index++) {
            chest.setItem(index, new ItemStack(subjects[index]));
        }
        var eyeLine = Vec3.atBottomCenterOf(test.absolutePos(BOOKCASE.north().north()));
        player.moveTo(eyeLine.x, eyeLine.y, eyeLine.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(test.absolutePos(BOOKCASE)));
        player.setItemInHand(InteractionHand.OFF_HAND, thaumometer);
        var target = ThaumometerItem.resolveTarget(test.getLevel(), player);
        test.assertTrue(target instanceof ScannedBlock block && block.pos().equals(test.absolutePos(BOOKCASE)),
                "The Thaumometer raycast did not hit the placed chest");

        var magic = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.PRAECANTATIO));
        var mind = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.COGNITIO));
        scanChest(test, player, thaumometer, 1);
        test.assertTrue(AspectPoolAccess.amount(player, magic) == 0 && AspectPoolAccess.amount(player, mind) == 0,
                "Releasing immediately granted scan points");
        scanChest(test, player, thaumometer, ThaumometerItem.SCAN_COMPLETE_ELAPSED_TICKS);
        int magicPoints = AspectPoolAccess.amount(player, magic);
        int mindPoints = AspectPoolAccess.amount(player, mind);
        test.assertTrue(magicPoints >= 2 && mindPoints >= 2, "The completed scan did not grant both required aspects");

        scanChest(test, player, thaumometer, ThaumometerItem.SCAN_COMPLETE_ELAPSED_TICKS);
        test.assertTrue(AspectPoolAccess.amount(player, magic) == magicPoints
                        && AspectPoolAccess.amount(player, mind) == mindPoints,
                "Rescanning the same inventory awarded duplicate points");

        var firstSteps = TTIds.rl("first_steps");
        var research = PlayerResearchAccess.of(player);
        test.assertTrue(ResearchManager.advanceStage(player, firstSteps) && research.hasCompleted(firstSteps),
                "The earned scan points did not complete First Steps");
        test.assertTrue(AspectPoolAccess.amount(player, magic) == magicPoints - 2
                        && AspectPoolAccess.amount(player, mind) == mindPoints - 2,
                "Completing First Steps charged the wrong observation cost");
    }

    private static void scanChest(GameTestHelper test, FakePlayer player, ItemStack thaumometer, int heldTicks) {
        var scanner = (ThaumometerItem) thaumometer.getItem();
        test.assertTrue(scanner.use(test.getLevel(), player, InteractionHand.OFF_HAND).getResult()
                        == InteractionResult.CONSUME,
                "The Thaumometer did not begin scanning the chest");
        scanner.releaseUsing(thaumometer, test.getLevel(), player, scanner.getUseDuration(thaumometer) - heldTicks);
        player.stopUsingItem();
    }

    private static void makeCrucible(GameTestHelper test, FakePlayer player, ItemStack dust) {
        var alchemy = TTIds.rl("unlock_alchemy");
        test.assertTrue(ResearchManager.unlockRequested(player, alchemy), "First Steps did not open alchemy research");
        test.assertTrue(ResearchManager.advanceStage(player, alchemy), "The scan did not supply the alchemy observations");
        test.assertFalse(ResearchManager.advanceStage(player, alchemy), "Alchemy advanced without a crucible");
        test.setBlock(CRUCIBLE.below(), Blocks.MAGMA_BLOCK);
        test.setBlock(CRUCIBLE, Blocks.CAULDRON);
        test.assertTrue(useDust(test, player, CRUCIBLE).consumesAction() && dust.isEmpty(),
                "The cauldron did not accept the final Salis Mundus");
    }

    private static BlockEntityCrucible crucible(GameTestHelper test) {
        var entity = test.getBlockEntity(CRUCIBLE);
        test.assertTrue(entity instanceof BlockEntityCrucible, "The transformed crucible has no block entity");
        return (BlockEntityCrucible) entity;
    }

    private static void fillCrucible(GameTestHelper test, FakePlayer player) {
        var alchemy = TTIds.rl("unlock_alchemy");
        test.assertTrue(ResearchManager.advanceStage(player, alchemy)
                        && PlayerResearchAccess.of(player).stageOf(alchemy) == 2,
                "Transforming the cauldron did not satisfy the crucible requirement");
        var inventory = player.getInventory();
        inventory.selected = 8;
        test.assertTrue(player.getMainHandItem().isEmpty(), "Expected an empty hotbar slot for the bucket");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        test.assertTrue(clickCrucible(test, player).consumesAction()
                        && player.getMainHandItem().is(Items.BUCKET)
                        && crucible(test).getTank().getFluidAmount() == 1000,
                "The survival bucket did not fill the crucible through its Forge fluid handler");
        var bucket = inventory.removeItem(inventory.selected, 1);
        test.assertTrue(inventory.add(bucket), "The player could not store the empty bucket");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COAL));
        test.assertTrue(!clickCrucible(test, player).consumesAction()
                        && player.getMainHandItem().is(Items.COAL) && crucible(test).getAspects().isEmpty(),
                "The cold crucible consumed the coal");
    }

    private static void craftNitor(GameTestHelper test, FakePlayer player) {
        var crucible = crucible(test);
        test.assertTrue(clickCrucible(test, player).consumesAction() && player.getMainHandItem().isEmpty(),
                "The boiling crucible did not dissolve the coal");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TORCH, 3));
        for (int count = 2; count >= 0; count--) {
            test.assertTrue(clickCrucible(test, player).consumesAction() && player.getMainHandItem().getCount() == count,
                    "A torch interaction did not dissolve exactly one item");
        }
        var fire = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.IGNIS));
        var power = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.POTENTIA));
        var light = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.LUX));
        var contents = crucible.getAspects();
        test.assertTrue(contents.amountOf(fire) == 10 && contents.amountOf(power) == 10 && contents.amountOf(light) == 15
                        && crucible.getTank().getFluidAmount() == 1000,
                "Dissolving coal and torches changed the water or supplied the wrong aspects");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLOWSTONE_DUST));
        test.assertTrue(clickCrucible(test, player).consumesAction() && player.getMainHandItem().isEmpty(),
                "The boiling crucible did not accept the Nitor catalyst");
        contents = crucible.getAspects();
        test.assertTrue(contents.totalAmount() == 5 && contents.amountOf(light) == 5
                        && crucible.getTank().getFluidAmount() == 950,
                "Nitor crafting consumed the wrong aspects or water");
        var research = PlayerResearchAccess.of(player);
        var alchemy = TTIds.rl("unlock_alchemy");
        test.assertTrue(research.knows(ResearchManager.craftedKey(TTIds.rl("nitor_yellow")))
                        && ResearchManager.advanceStage(player, alchemy) && research.hasCompleted(alchemy),
                "Crafting Nitor did not satisfy the final alchemy stage");
    }

    private static InteractionResult clickCrucible(GameTestHelper test, FakePlayer player) {
        var pos = test.absolutePos(CRUCIBLE);
        var state = test.getBlockState(CRUCIBLE);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
        return state.use(test.getLevel(), player, InteractionHand.MAIN_HAND, hit);
    }

    private static InteractionResult useDust(GameTestHelper test, FakePlayer player, BlockPos target) {
        BlockPos pos = test.absolutePos(target);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        return player.getItemInHand(DUST_HAND).useOn(new UseOnContext(player, DUST_HAND, hit));
    }

    private static void pickUpCrystal(GameTestHelper test, FakePlayer player) {
        var crystal = crystal(test, TTAspects.ORDO, 1);
        var expected = crystal.copy();
        var drop = new ItemEntity(test.getLevel(), player.getX(), player.getY(), player.getZ(), crystal);
        drop.setNoPickUpDelay();
        test.getLevel().addFreshEntity(drop);
        drop.playerTouch(player);
        test.assertTrue(drop.isRemoved() && player.getInventory().contains(expected),
                "Native item pickup did not move the crystal to the inventory");
    }

    private static ItemStack crystal(GameTestHelper test, ResourceKey<IAspect> aspect, int count) {
        var holder = Objects.requireNonNull(Aspects.resolve(test.getLevel(), aspect));
        var crystal = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get(), count);
        ItemData.set(crystal, TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(holder, 1));
        return crystal;
    }

    private static void wakeInBed(GameTestHelper test, FakePlayer player) {
        var bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        test.setBlock(BED.below(), Blocks.SMOOTH_STONE);
        test.setBlock(BED.south().below(), Blocks.SMOOTH_STONE);
        test.setBlock(BED.south(), bed.setValue(BedBlock.PART, BedPart.FOOT));
        test.setBlock(BED, bed.setValue(BedBlock.PART, BedPart.HEAD));
        player.startSleeping(test.absolutePos(BED));
        player.stopSleepInBed(true, true);
    }

    static FakePlayer survivalPlayer(GameTestHelper test) {
        var player = new FakePlayer(test.getLevel(), new GameProfile(UUID.randomUUID(), "ThaumaturgeTest"));
        player.setGameMode(GameType.SURVIVAL);
        var spawn = test.absolutePos(CENTER.north());
        player.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        return player;
    }
}
