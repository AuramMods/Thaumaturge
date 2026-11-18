package com.leclowndu93150.thaumaturge.content.device.sprayer;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingAccessor;
import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingMixAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;

public class BrewingRoutesRuntimeTest {
    private Holder<Potion> holder(Potion potion) { return BuiltInRegistries.POTION.wrapAsHolder(potion); }
    private List<Item> items(List<ItemStack> stacks) { return stacks.stream().map(ItemStack::getItem).toList(); }
    private Potion potion(String name) {
        var potion = new Potion();
        ForgeRegistries.POTIONS.register(new ResourceLocation("port_fixture", name), potion);
        return potion;
    }
    private Object mix(Potion from, Ingredient ingredient, Potion to) {
        return new PotionBrewing.Mix<>(ForgeRegistries.POTIONS, from, ingredient, to);
    }

    @Test
    void actualStaticNativeTableAndMixAccessorsAgreeWithRealVanillaBrewing() {
        var table = PotionBrewingAccessor.thaumaturge$getPotionMixes();
        assertFalse(table.isEmpty());
        var first = (PotionBrewing.Mix<?>) table.get(0);
        var access = (PotionBrewingMixAccessor) first;
        assertSame(first.from, access.thaumaturge$getFrom());
        assertSame(first.to, access.thaumaturge$getTo());
        assertSame(first.ingredient, access.thaumaturge$getIngredient());
        var awkward = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
        var result = PotionBrewing.mix(new ItemStack(Items.SUGAR), awkward);
        assertSame(Potions.SWIFTNESS, PotionUtils.getPotion(result));
        assertEquals(List.of(Items.SUGAR, Items.NETHER_WART), items(BrewingRoutes.reagents(holder(Potions.SWIFTNESS))));
        assertTrue(BrewingRoutes.reagents(holder(Potions.WATER)).isEmpty());
    }

    @Test
    void actualVanillaAlternativeRoutesDeduplicateReagentsAndExcludeContainerConversions() {
        var reagents = items(BrewingRoutes.reagents(holder(Potions.SLOWNESS)));
        assertTrue(reagents.containsAll(List.of(Items.FERMENTED_SPIDER_EYE, Items.NETHER_WART, Items.SUGAR, Items.RABBIT_FOOT)));
        assertEquals(1, reagents.stream().filter(item -> item == Items.FERMENTED_SPIDER_EYE).count());
        assertFalse(reagents.contains(Items.GUNPOWDER));
        assertFalse(reagents.contains(Items.DRAGON_BREATH));
        var returned = BrewingRoutes.reagents(holder(Potions.SWIFTNESS));
        returned.get(0).setCount(12);
        assertEquals(1, BrewingRoutes.reagents(holder(Potions.SWIFTNESS)).get(0).getCount());
    }

    @Test
    void nativeCustomMixCyclesStopAtVisitedKeysAndKeepFirstItemSelection() {
        var a = potion("route_cycle_a");
        var b = potion("route_cycle_b");
        var alternatives = Ingredient.of(Items.REDSTONE, Items.GLOWSTONE_DUST);
        var mixes = List.of(mix(a, alternatives, b), mix(b, Ingredient.of(Items.SUGAR), a));
        assertEquals(List.of(alternatives.getItems()[0].getItem(), Items.SUGAR), items(BrewingRoutes.reagents(mixes, holder(b))));
        assertTrue(BrewingRoutes.reagents(mixes, Holder.direct(b)).isEmpty());
    }

    @Test
    void nativeCustomMixTraversalKeepsTheSourceDepthLimit() {
        List<Item> ingredients = List.of(Items.COAL, Items.IRON_INGOT, Items.GOLD_INGOT, Items.STICK, Items.APPLE,
                Items.BREAD, Items.CARROT, Items.POTATO, Items.BEETROOT, Items.WHEAT, Items.SUGAR, Items.REDSTONE,
                Items.GLOWSTONE_DUST, Items.DIAMOND, Items.EMERALD, Items.LAPIS_LAZULI, Items.BONE, Items.FLINT);
        List<Object> mixes = new ArrayList<>();
        Potion previous = Potions.WATER;
        for (int i = 0; i < ingredients.size(); i++) {
            var next = potion("route_depth_" + i);
            mixes.add(mix(previous, Ingredient.of(ingredients.get(i)), next));
            previous = next;
        }
        var result = items(BrewingRoutes.reagents(mixes, holder(previous)));
        assertEquals(17, result.size());
        assertSame(Items.FLINT, result.get(0));
        assertSame(Items.IRON_INGOT, result.get(16));
        assertFalse(result.contains(Items.COAL));
    }

    @Test
    void emptyIngredientsAndRepeatedIngredientItemsKeepTheOriginalTraversalRules() {
        var a = potion("route_empty_a");
        var b = potion("route_empty_b");
        var c = potion("route_empty_c");
        var mixes = List.of(mix(Potions.WATER, Ingredient.of(Items.SUGAR), a),
                mix(a, Ingredient.EMPTY, b), mix(b, Ingredient.of(Items.SUGAR), c));
        assertEquals(List.of(Items.SUGAR), items(BrewingRoutes.reagents(mixes, holder(c))));
    }
}
