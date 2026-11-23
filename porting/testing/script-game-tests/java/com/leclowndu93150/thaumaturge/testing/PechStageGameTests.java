package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.leclowndu93150.thaumaturge.content.pech.MenuPech;
import com.leclowndu93150.thaumaturge.content.pech.PechBarter;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class PechStageGameTests {
    private static final String STAGE = "barter_with_pech";

    private PechStageGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_pech")
    public static void taggedPechTableControlsTradeCostAndPayout(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var pech = test.spawnWithNoFreeWill(TTEntities.PECH.get(), new BlockPos(2, 2, 2));
        pech.assignVariant(EntityPech.TYPE_FORAGER);
        pech.setDomesticated(true);
        var menu = new MenuPech(1, actor.getInventory(), pech);
        var offer = new ItemStack(Items.AMETHYST_SHARD, 2);
        try {
            menu.getSlot(0).set(offer);
            test.assertTrue(menu.canTrade() && pech.getValue(offer) == 3,
                    "The scripted offer value did not reach the Pech menu");
            menu.clickMenuButton(actor, MenuPech.TRADE_BUTTON_ID);
            test.assertTrue(menu.getSlot(0).getItem().getCount() == 2 && menu.getSlot(1).getItem().isEmpty()
                            && PechBarter.haggle(pech, offer, RandomSource.create(2), level.registryAccess()).isEmpty(),
                    "A missing stage allowed a menu trade or actorless payout");
            GameStageHelper.getPlayerData(actor).addStage(STAGE);
            pech.getRandom().setSeed(2);
            menu.clickMenuButton(actor, MenuPech.TRADE_BUTTON_ID);
            var paidOut = menu.getSlot(1).getItem();
            test.assertTrue(menu.getSlot(0).getItem().getCount() == 1 && paidOut.is(Items.PRISMARINE_SHARD)
                            && paidOut.getCount() == 2 && menu.getSlot(2).getItem().isEmpty(),
                    "One offered item did not pay out exactly two prismarine shards from the tagged table");
        } finally {
            menu.getSlot(0).set(ItemStack.EMPTY);
            menu.getSlot(1).set(ItemStack.EMPTY);
            menu.removed(actor);
            pech.discard();
            StageGameTests.remove(test, actor);
        }
        test.succeed();
    }
}
