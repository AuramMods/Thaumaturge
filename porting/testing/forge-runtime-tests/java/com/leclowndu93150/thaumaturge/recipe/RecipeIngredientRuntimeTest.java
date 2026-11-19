package com.leclowndu93150.thaumaturge.recipe;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.data.recipe.RecipeDataOutput;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RecipeIngredientRuntimeTest {
    @Test
    void packagedTagsProvideVanillaIngredientsAndUseOptionalForgeMetalTags(@TempDir Path temporary)
            throws Exception {
        var source = Path.of(System.getProperty("thaumaturge.testResourceRoot"));
        var isolatedTags = temporary.resolve("thaumaturge");
        var optionalMetals = temporary.resolve("metals");
        for (var tag : List.<TagKey<?>>of(TTItemTags.CRAFTING_TABLES, TTItemTags.FURNACES,
                TTItemTags.CACTUS, TTItemTags.SUGAR_CANE, TTItemTags.EMPTY_BUCKETS, TTItemTags.WATER_BUCKETS,
                TTItemTags.MILK_BUCKETS, TTItemTags.STONES, TTItemTags.NUGGETS_COPPER, TTItemTags.NUGGETS_SILVER,
                TTBlockTags.CRAFTING_TABLES, TTBlockTags.STONES)) {
            var id = tag.location();
            var relative = Path.of("data", id.getNamespace(), TagManager.getTagDir(tag.registry()), id.getPath() + ".json");
            var destination = isolatedTags.resolve(relative);
            Files.createDirectories(destination.getParent());
            Files.copy(source.resolve(relative), destination);
        }
        var items = new TagLoader<>(BuiltInRegistries.ITEM::getOptional, "tags/items");
        var blocks = new TagLoader<>(BuiltInRegistries.BLOCK::getOptional, "tags/blocks");
        try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new PathPackResources("thaumaturge", isolatedTags, false)))) {
            var tags = items.loadAndBuild(manager);
            assertEquals(Set.of(Items.CRAFTING_TABLE), Set.copyOf(tags.get(TTItemTags.CRAFTING_TABLES.location())));
            assertEquals(Set.of(Items.FURNACE), Set.copyOf(tags.get(TTItemTags.FURNACES.location())));
            assertEquals(Set.of(Items.CACTUS), Set.copyOf(tags.get(TTItemTags.CACTUS.location())));
            assertEquals(Set.of(Items.SUGAR_CANE), Set.copyOf(tags.get(TTItemTags.SUGAR_CANE.location())));
            assertEquals(Set.of(Items.BUCKET), Set.copyOf(tags.get(TTItemTags.EMPTY_BUCKETS.location())));
            assertEquals(Set.of(Items.WATER_BUCKET), Set.copyOf(tags.get(TTItemTags.WATER_BUCKETS.location())));
            assertEquals(Set.of(Items.MILK_BUCKET), Set.copyOf(tags.get(TTItemTags.MILK_BUCKETS.location())));
            assertEquals(Set.of(Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.STONE, Items.DEEPSLATE, Items.TUFF),
                    Set.copyOf(tags.get(TTItemTags.STONES.location())));
            assertTrue(tags.get(TTItemTags.NUGGETS_COPPER.location()).isEmpty());
            assertTrue(tags.get(TTItemTags.NUGGETS_SILVER.location()).isEmpty());
            var blockTags = blocks.loadAndBuild(manager);
            assertEquals(Set.of(Blocks.CRAFTING_TABLE), Set.copyOf(blockTags.get(TTBlockTags.CRAFTING_TABLES.location())));
            assertEquals(Set.of(Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.STONE, Blocks.DEEPSLATE, Blocks.TUFF),
                    Set.copyOf(blockTags.get(TTBlockTags.STONES.location())));
        }

        Path forgeNuggets = Files.createDirectories(optionalMetals.resolve("data/forge/tags/items/nuggets"));
        Files.writeString(forgeNuggets.resolve("copper.json"), "{\"values\":[\"minecraft:iron_nugget\"]}");
        Files.writeString(forgeNuggets.resolve("silver.json"), "{\"values\":[\"minecraft:gold_nugget\"]}");
        Path forgeBlocks = Files.createDirectories(optionalMetals.resolve("data/forge/tags/blocks"));
        Files.writeString(forgeBlocks.resolve("stone.json"), "{\"values\":[\"minecraft:polished_andesite\"]}");
        try (var manager = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(
                new PathPackResources("thaumaturge", isolatedTags, false),
                new PathPackResources("metal-fixture", optionalMetals, false)))) {
            var tags = items.loadAndBuild(manager);
            assertEquals(Set.of(Items.IRON_NUGGET), Set.copyOf(tags.get(TTItemTags.NUGGETS_COPPER.location())));
            assertEquals(Set.of(Items.GOLD_NUGGET), Set.copyOf(tags.get(TTItemTags.NUGGETS_SILVER.location())));
            assertTrue(blocks.loadAndBuild(manager).get(TTBlockTags.STONES.location()).contains(Blocks.POLISHED_ANDESITE));
        }
    }

    @Test
    void stackedCookingRecipesPreserveResultsAndUnlocksThroughNativeDatagen(@TempDir Path output) throws Exception {
        var smelting = ResourceLocation.fromNamespaceAndPath("thaumaturge", "iron_ingot_from_cluster_fixture");
        var blasting = ResourceLocation.fromNamespaceAndPath("thaumaturge", "iron_ingot_blasting_from_cluster_fixture");
        var provider = new RecipeProvider(new PackOutput(output)) {
            @Override
            protected void buildRecipes(Consumer<FinishedRecipe> recipes) {
                var result = new ItemStack(Items.IRON_INGOT, 2);
                result.getOrCreateTag().putString("port_fixture", "stacked_result");
                var sink = RecipeDataOutput.withStackResult(recipes, result);
                var ingredient = Ingredient.of(Items.RAW_IRON);
                var criterion = InventoryChangeTrigger.TriggerInstance.hasItems(Items.RAW_IRON);
                SimpleCookingRecipeBuilder.smelting(ingredient, RecipeCategory.MISC, Items.IRON_INGOT, 1F, 200)
                        .group("iron_ingot").unlockedBy("has", criterion).save(sink, smelting);
                SimpleCookingRecipeBuilder.blasting(ingredient, RecipeCategory.MISC, Items.IRON_INGOT, 1F, 100)
                        .group("iron_ingot").unlockedBy("has", criterion).save(sink, blasting);
            }
        };
        provider.run(CachedOutput.NO_CACHE).join();

        var smeltingRecipe = assertInstanceOf(SmeltingRecipe.class, load(output, smelting));
        verifyCooking(smeltingRecipe, 200, new FurnaceBlockEntity(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState()));
        var blastingRecipe = assertInstanceOf(BlastingRecipe.class, load(output, blasting));
        verifyCooking(blastingRecipe, 100,
                new BlastFurnaceBlockEntity(BlockPos.ZERO, Blocks.BLAST_FURNACE.defaultBlockState()));
    }

    private static Recipe<?> load(Path output, ResourceLocation id) throws Exception {
        var recipe = JsonParser.parseString(Files.readString(output.resolve("data/thaumaturge/recipes/" + id.getPath() + ".json")))
                .getAsJsonObject();
        var unlock = JsonParser.parseString(Files.readString(
                output.resolve("data/thaumaturge/advancements/recipes/misc/" + id.getPath() + ".json")))
                .getAsJsonObject();
        assertEquals(id.toString(), unlock.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString());
        assertTrue(unlock.getAsJsonObject("criteria").has("has"));
        return RecipeManager.fromJson(id, recipe);
    }

    private static void verifyCooking(AbstractCookingRecipe recipe, int time, AbstractFurnaceBlockEntity furnace)
            throws Exception {
        assertEquals(time, recipe.getCookingTime());
        assertEquals(1F, recipe.getExperience());
        assertEquals("iron_ingot", recipe.getGroup());
        assertTrue(recipe.getIngredients().get(0).test(new ItemStack(Items.RAW_IRON)));
        var burn = AbstractFurnaceBlockEntity.class.getDeclaredMethod(
                "burn", RegistryAccess.class, Recipe.class, NonNullList.class, int.class);
        burn.setAccessible(true);
        var slots = NonNullList.withSize(3, ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.RAW_IRON, 2));
        assertEquals(true, burn.invoke(furnace, RegistryAccess.EMPTY, recipe, slots, 64));
        assertEquals(1, slots.get(0).getCount());
        assertTrue(slots.get(2).is(Items.IRON_INGOT));
        assertEquals(2, slots.get(2).getCount());
        assertEquals("stacked_result", slots.get(2).getTag().getString("port_fixture"));
        assertEquals(true, burn.invoke(furnace, RegistryAccess.EMPTY, recipe, slots, 64));
        assertTrue(slots.get(0).isEmpty());
        assertEquals(4, slots.get(2).getCount());
    }
}
