package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class ContentTagGameTests {
    private ContentTagGameTests() {}

    @GameTest(template = "essentia_room")
    public static void worldgenTagsKeepTheirOriginalBiomeBoundaries(GameTestHelper test) {
        var biomes = test.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        var plains = biomes.getHolderOrThrow(Biomes.PLAINS);
        var snowyPlains = biomes.getHolderOrThrow(Biomes.SNOWY_PLAINS);
        var meadow = biomes.getHolderOrThrow(Biomes.MEADOW);
        test.assertTrue(plains.is(TTBiomeTags.HAS_GREATWOOD_RARE) && plains.is(TTBiomeTags.HAS_MOUND)
                && plains.is(TTBiomeTags.HAS_ELDRITCH_OBELISK), "Plains should support all three features");
        test.assertTrue(!snowyPlains.is(TTBiomeTags.HAS_GREATWOOD_RARE) && !snowyPlains.is(TTBiomeTags.HAS_MOUND)
                && snowyPlains.is(TTBiomeTags.HAS_ELDRITCH_OBELISK), "Only obelisks belong in Snowy Plains");
        test.assertTrue(!meadow.is(TTBiomeTags.HAS_GREATWOOD_RARE) && meadow.is(TTBiomeTags.HAS_MOUND)
                && !meadow.is(TTBiomeTags.HAS_ELDRITCH_OBELISK), "Only mounds belong in Meadows");
        test.succeed();
    }

    @GameTest(template = "essentia_room")
    public static void arcaneStoneAndTaintUseTheOriginalStoneGroup(GameTestHelper test) {
        BlockPos pos = new BlockPos(2, 2, 2);
        test.setBlock(pos, Blocks.CRAFTING_TABLE);
        var player = new FakePlayer(test.getLevel(), new GameProfile(UUID.randomUUID(), "StoneTagTest"));
        var menu = new CraftingMenu(1, player.getInventory(),
                ContainerLevelAccess.create(test.getLevel(), test.absolutePos(pos)));
        try {
            for (int i = 1; i <= 9; i++) {
                menu.getSlot(i).set(i == 5 ? new ItemStack(TTItems.ESSENTIA_CRYSTAL.get()) : new ItemStack(Blocks.STONE));
            }
            var output = menu.getSlot(0).getItem();
            test.assertTrue(output.is(TTItems.STONE_ARCANE.get()) && output.getCount() == 8,
                    "Natural stone should give eight Arcane Stone blocks");
            test.assertTrue(Blocks.STONE.defaultBlockState().is(TTBlockTags.TAINT_CONVERTIBLE_ROCK),
                    "Taint should still be able to convert natural stone");
            for (var block : List.of(Blocks.INFESTED_DEEPSLATE, Blocks.INFESTED_STONE, Blocks.POLISHED_ANDESITE,
                    Blocks.POLISHED_DEEPSLATE, Blocks.POLISHED_DIORITE, Blocks.POLISHED_GRANITE)) {
                menu.getSlot(1).set(new ItemStack(block));
                test.assertTrue(menu.getSlot(0).getItem().isEmpty(),
                        "Arcane Stone should reject " + block.getName().getString());
                test.assertTrue(!block.defaultBlockState().is(TTBlockTags.TAINT_CONVERTIBLE_ROCK),
                        "Taint should leave " + block.getName().getString() + " intact");
            }
        } finally {
            menu.removed(player);
        }
        test.succeed();
    }
}
