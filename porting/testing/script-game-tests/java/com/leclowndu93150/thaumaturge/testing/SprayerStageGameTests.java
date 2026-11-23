package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.device.sprayer.BlockEntityPotionSprayer;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Objects;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class SprayerStageGameTests {
    private static final BlockPos CORE = new BlockPos(2, 2, 2);

    private SprayerStageGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_sprayer")
    public static void sprayerUsesNativeBrewingAndRetainsAStagedDose(GameTestHelper test) {
        var level = test.getLevel();
        var actor = StageGameTests.player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        var aqua = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AQUA));
        var alkimia = Objects.requireNonNull(Aspects.resolve(level, TTAspects.ALKIMIA));
        var block = TTBlocks.POTION_SPRAYER.get();
        var state = block.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST);
        test.setBlock(CORE, state);
        var sprayer = (BlockEntityPotionSprayer) test.getBlockEntity(CORE);
        block.setPlacedBy(level, test.absolutePos(CORE), state, actor, new ItemStack(block));
        var reagent = new ItemStack(Items.DIAMOND);
        var brewed = BrewingRecipeRegistry.getOutput(
                PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), reagent);
        test.assertTrue(!brewed.isEmpty() && PotionUtils.getPotion(brewed) == Potions.LUCK,
                "The scripted recipe did not brew the expected potion through Forge");
        sprayer.setPotion(brewed);
        int alkimiaInput = AspectIndexAccess.of(reagent).amountOf(alkimia) + 3;
        test.assertTrue(sprayer.essentiaNeeded().amountOf(alkimia) == alkimiaInput - alkimiaInput * 66 / 100,
                "The sprayer did not derive its essentia from the scripted brewing reagent");

        var speed = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS);
        sprayer.setPotion(speed);
        test.assertTrue(sprayer.essentiaNeeded().totalAmount() == 3 && sprayer.essentiaNeeded().amountOf(aqua) == 3
                        && sprayer.insert(aqua, 3, Direction.DOWN, false) == 0,
                "The potion rule or stage restriction did not reach the essentia intake");
        stages.addStage("spray_swiftness");
        test.assertTrue(sprayer.insert(aqua, 5, Direction.DOWN, false) == 3,
                "A dose did not accept precisely three Aqua");
        var targetPos = CORE.east(2);
        test.setBlock(targetPos.below(), Blocks.SMOOTH_STONE);
        var target = test.spawnWithNoFreeWill(EntityType.PIG, targetPos);
        test.startSequence().thenWaitUntil(() -> test.assertTrue(sprayer.sprayShots() == 1,
                "Waiting for the 3-Aqua dose")).thenExecute(() -> {
            stages.removeStage("spray_swiftness");
            sprayer.load(sprayer.saveWithFullMetadata());
            test.setBlock(CORE, state.setValue(BlockStateProperties.ENABLED, false));
        }).thenIdle(6).thenExecute(() -> {
            test.assertTrue(sprayer.sprayShots() == 1 && !target.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "A revoked owner triggered or lost the saved dose");
            test.setBlock(CORE, state);
            stages.addStage("spray_swiftness");
        }).thenIdle(2).thenExecute(() -> test.setBlock(CORE, state.setValue(BlockStateProperties.ENABLED, false)))
                .thenWaitUntil(() -> test.assertTrue(sprayer.sprayShots() == 0
                                && target.hasEffect(MobEffects.MOVEMENT_SPEED),
                        "Waiting for one dose, shots=" + sprayer.sprayShots() + ", speed="
                                + target.hasEffect(MobEffects.MOVEMENT_SPEED)))
                .thenExecute(() -> StageGameTests.remove(test, actor)).thenSucceed();
    }
}
