package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.BlockEntityArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import com.mojang.authlib.GameProfile;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class ResearchGameTests {
    private static final String ROOM = "essentia_room";
    private static final BlockPos CENTER = new BlockPos(2, 1, 2);

    private ResearchGameTests() {}

    @GameTest(template = ROOM)
    public static void crystalPickupAndWakeAwardJournalOnce(GameTestHelper test) {
        var player = survivalPlayer(test);
        var research = PlayerResearchAccess.of(player);
        var order = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.ORDO));
        var crystal = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get());
        ItemData.set(crystal, TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(order, 1));

        test.assertFalse(research.knows(TTIds.rl("gotcrystals")), "New player already knew the crystal discovery");
        var expectedCrystal = crystal.copy();
        var drop = new ItemEntity(test.getLevel(), player.getX(), player.getY(), player.getZ(), crystal);
        drop.setNoPickUpDelay();
        test.getLevel().addFreshEntity(drop);
        drop.playerTouch(player);
        test.assertTrue(drop.isRemoved() && player.getInventory().contains(expectedCrystal),
                "Native item pickup did not move the crystal to the inventory");
        test.assertTrue(research.hasCompleted(TTIds.rl("gotcrystals")), "Crystal pickup did not complete the discovery");
        test.assertFalse(research.knows(TTIds.rl("gotdream")), "Dream unlocked before waking");

        var bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        test.setBlock(CENTER.below(), Blocks.SMOOTH_STONE);
        test.setBlock(CENTER.south().below(), Blocks.SMOOTH_STONE);
        test.setBlock(CENTER.south(), bed.setValue(BedBlock.PART, BedPart.FOOT));
        test.setBlock(CENTER, bed.setValue(BedBlock.PART, BedPart.HEAD));
        player.startSleeping(test.absolutePos(CENTER));
        player.stopSleepInBed(true, true);
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

        player.startSleeping(test.absolutePos(CENTER));
        player.stopSleepInBed(true, true);
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

    private static FakePlayer survivalPlayer(GameTestHelper test) {
        var player = new FakePlayer(test.getLevel(), new GameProfile(UUID.randomUUID(), "ThaumaturgeTest"));
        player.setGameMode(GameType.SURVIVAL);
        var spawn = test.absolutePos(CENTER.north());
        player.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        return player;
    }
}
