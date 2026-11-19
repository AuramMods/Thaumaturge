package com.leclowndu93150.thaumaturge.client.render.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.junit.jupiter.api.Test;

class RecipePreviewRuntimeTest {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("thaumaturge", "preview_test");

    @Test
    void smithingTransformKeepsTheDisplayedArmorData() {
        var template = new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        var armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        armor.setHoverName(Component.literal("Research gear"));
        armor.getOrCreateTag().putInt("preview_marker", 42);
        var ingot = new ItemStack(Items.NETHERITE_INGOT);
        var recipe = new SmithingTransformRecipe(ID, Ingredient.of(template), Ingredient.of(armor), Ingredient.of(ingot),
                new ItemStack(Items.NETHERITE_CHESTPLATE));

        var output = RecipePreview.smithingResult(recipe, RegistryAccess.EMPTY, template, armor, ingot);

        assertTrue(output.is(Items.NETHERITE_CHESTPLATE));
        assertEquals("Research gear", output.getHoverName().getString());
        assertEquals(42, output.getTag().getInt("preview_marker"));
        assertTrue(armor.is(Items.DIAMOND_CHESTPLATE));
    }

    @Test
    void smithingTrimResolvesTheActiveRegistryAndKeepsTheSourceUntrimmed() {
        var template = new ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
        var armor = new ItemStack(Items.IRON_CHESTPLATE);
        var ingot = new ItemStack(Items.AMETHYST_SHARD);
        var materials = new MappedRegistry<TrimMaterial>(Registries.TRIM_MATERIAL, Lifecycle.stable());
        var material = materials.register(ResourceKey.create(Registries.TRIM_MATERIAL, ID),
                TrimMaterial.create("preview_amethyst", ingot.getItem(), 0.2F, Component.literal("Amethyst"), Map.of()),
                Lifecycle.stable());
        var patterns = new MappedRegistry<TrimPattern>(Registries.TRIM_PATTERN, Lifecycle.stable());
        var pattern = patterns.register(ResourceKey.create(Registries.TRIM_PATTERN, ID),
                new TrimPattern(ID, template.getItemHolder(), Component.literal("Preview pattern")), Lifecycle.stable());
        var registries = new RegistryAccess.ImmutableRegistryAccess(List.of(materials.freeze(), patterns.freeze())).freeze();
        var recipe = new SmithingTrimRecipe(ID, Ingredient.of(template), Ingredient.of(armor), Ingredient.of(ingot));

        var items = BuiltInRegistries.ITEM;
        var originalTags = new LinkedHashMap<TagKey<Item>, List<Holder<Item>>>();
        items.getTags().forEach(pair -> originalTags.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var trimmable = new ArrayList<>(originalTags.computeIfAbsent(ItemTags.TRIMMABLE_ARMOR, tag -> List.of()));
        if (!trimmable.contains(armor.getItemHolder())) {
            trimmable.add(armor.getItemHolder());
        }
        var tags = new LinkedHashMap<>(originalTags);
        tags.put(ItemTags.TRIMMABLE_ARMOR, trimmable);
        items.bindTags(tags);
        try {
            var output = RecipePreview.smithingResult(recipe, registries, template, armor, ingot);

            assertTrue(output.is(Items.IRON_CHESTPLATE));
            assertFalse(ArmorTrim.getTrim(registries, armor).isPresent());
            var trim = ArmorTrim.getTrim(registries, output).orElseThrow();
            assertSame(pattern, trim.pattern());
            assertSame(material, trim.material());
        } finally {
            items.bindTags(originalTags);
        }
    }

    @Test
    void smithingUsesDeclaredOutputWhenAssemblyHasNoResultAndKeepsTheDisplayInputs() {
        var template = new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        var armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        var ingot = new ItemStack(Items.NETHERITE_INGOT, 3);
        var recipe = mock(SmithingRecipe.class);
        var declared = new ItemStack(Items.NETHERITE_CHESTPLATE);
        when(recipe.getResultItem(RegistryAccess.EMPTY)).thenReturn(declared);
        when(recipe.assemble(any(Container.class), same(RegistryAccess.EMPTY))).thenAnswer(call -> {
            Container inputs = call.getArgument(0);
            assertEquals(3, inputs.getContainerSize());
            assertTrue(inputs.getItem(0).is(template.getItem()));
            assertTrue(inputs.getItem(1).is(armor.getItem()));
            assertTrue(inputs.getItem(2).is(ingot.getItem()));
            inputs.getItem(0).shrink(1);
            inputs.getItem(1).setHoverName(Component.literal("Temporary"));
            inputs.getItem(2).shrink(2);
            return ItemStack.EMPTY;
        });

        assertSame(declared, RecipePreview.smithingResult(recipe, RegistryAccess.EMPTY, template, armor, ingot));
        assertSame(declared, RecipePreview.smithingResult(recipe, RegistryAccess.EMPTY, ItemStack.EMPTY, armor, ingot));
        assertEquals(1, template.getCount());
        assertFalse(armor.hasCustomHoverName());
        assertEquals(3, ingot.getCount());
    }

    @Test
    void previewWaitsForTheClientWorldRegistry() {
        var recipe = mock(SmithingRecipe.class);
        assertTrue(RecipePreview.resultOf(recipe, null).isEmpty());
        assertTrue(RecipePreview.smithingResult(recipe, null, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY).isEmpty());
        verifyNoInteractions(recipe);
    }
}
