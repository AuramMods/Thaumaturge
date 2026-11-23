package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.recipe.CraftingInput;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Objects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class SpecialCraftingGameTests {
    private SpecialCraftingGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_special_crafting")
    public static void relocatedSpecialRecipesKeepTheirDynamicOutputsAndRemainders(GameTestHelper test) {
        var level = test.getLevel();
        var ignis = Objects.requireNonNull(Aspects.resolve(level, TTAspects.IGNIS));
        var ordo = Objects.requireNonNull(Aspects.resolve(level, TTAspects.ORDO));
        var terra = Objects.requireNonNull(Aspects.resolve(level, TTAspects.TERRA));
        var mix = CraftingInput.of(3, 2, List.of(new ItemStack(Items.BOWL), new ItemStack(Items.FLINT),
                new ItemStack(Items.REDSTONE), EssentiaCrystalAccess.create(ignis, 1),
                EssentiaCrystalAccess.create(ordo, 1), EssentiaCrystalAccess.create(terra, 1)));
        var salis = requireRecipe(test, "probe_salis_mundus", mix);
        var tools = salis.getRemainingItems(mix);
        test.assertTrue(salis.assemble(mix, level.registryAccess()).is(TTItems.SALIS_MUNDUS.get())
                        && tools.get(0).is(Items.BOWL) && tools.get(1).is(Items.FLINT)
                        && tools.stream().filter(stack -> !stack.isEmpty()).count() == 2,
                "Salis Mundus did not produce dust and return only the bowl and flint");

        var phial = PhialItem.makeFilled(ordo, 6);
        var filtering = CraftingInput.of(2, 1, List.of(new ItemStack(TTItems.LABEL.get()), phial));
        var labeling = requireRecipe(test, "probe_label_filter", filtering);
        var filtered = labeling.assemble(filtering, level.registryAccess());
        var returned = labeling.getRemainingItems(filtering);
        test.assertTrue(ItemStack.isSameItemSameTags(filtered, LabelItem.withAspect(ordo))
                        && returned.get(0).isEmpty() && ItemStack.matches(returned.get(1), phial),
                "Label filtering did not preserve the selected aspect and filled phial");

        var first = new ItemStack(TTItems.PRIMORDIAL_PEARL.get());
        first.setDamageValue(PrimordialPearlItem.MAX_DAMAGE - 2);
        var second = new ItemStack(TTItems.PRIMORDIAL_PEARL.get());
        second.setDamageValue(PrimordialPearlItem.MAX_DAMAGE - 3);
        var repair = CraftingInput.of(2, 1, List.of(first, second));
        var repairing = requireRecipe(test, "probe_primordial_pearl_repair", repair);
        var repaired = repairing.assemble(repair, level.registryAccess());
        test.assertTrue(repaired.is(TTItems.PRIMORDIAL_PEARL.get()) && repaired.getCount() == 1
                        && repaired.getDamageValue() == PrimordialPearlItem.MAX_DAMAGE - 5
                        && repairing.getRemainingItems(repair).stream().allMatch(ItemStack::isEmpty),
                "Pearl repair did not combine the two durability balances exactly once");
        test.succeed();
    }

    private static CraftingRecipe requireRecipe(GameTestHelper test, String id, CraftingInput input) {
        var recipe = (CraftingRecipe) test.getLevel().getRecipeManager()
                .byKey(new ResourceLocation("crafttweaker", id)).orElseThrow();
        test.assertTrue(recipe.matches(input, test.getLevel()), "The relocated recipe did not match " + id);
        return recipe;
    }
}
