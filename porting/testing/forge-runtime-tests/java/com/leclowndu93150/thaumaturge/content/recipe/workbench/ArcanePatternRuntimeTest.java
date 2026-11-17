package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.recipe.CraftingInput;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ArcanePatternRuntimeTest {
    private final int originalWidth = ArcaneShapedRecipePattern.maxWidth;
    private final int originalHeight = ArcaneShapedRecipePattern.maxHeight;

    @AfterEach void restoreCraftingSize() {
        ArcaneShapedRecipePattern.maxWidth = originalWidth;
        ArcaneShapedRecipePattern.maxHeight = originalHeight;
    }

    private record Grid(CraftingInput input) implements IArcaneCraftingInput {
        @Override public int size() { return input.size(); }
        @Override public ItemStack getItem(int slot) { return input.getItem(slot); }
        @Override public ItemStack getItem(int x, int y) { return input.getItem(x + y * input.width()); }
        @Override public int width() { return input.width(); }
        @Override public int height() { return input.height(); }
        @Override public Player player() { return null; }
        @Override public int ingredientCount() { return input.ingredientCount(); }
        @Override public List<ItemStack> items() { return input.items(); }
        @Override public StackedContents stackedContents() { return input.stackedContents(); }
        @Override public AspectList availableCrystals() { return AspectList.EMPTY; }
        @Override public ItemStack wandStack() { return ItemStack.EMPTY; }
    }

    private Grid grid(int width, int height, ItemStack... stacks) {
        return new Grid(CraftingInput.of(width, height, List.of(stacks)));
    }

    private ArcaneShapedRecipePattern parse(String json) {
        return ArcaneShapedRecipePattern.MAP_CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow();
    }

    @Test
    void actualPatternJsonTrimsAndMatchesBothOrientationsWithNativeIngredients() {
        var pattern = parse("{\"key\":{\"C\":{\"item\":\"minecraft:coal\"},\"I\":{\"item\":\"minecraft:iron_ingot\"}},\"pattern\":[\"   \",\" CI\",\"   \"]}");
        assertEquals(2, pattern.width());
        assertEquals(1, pattern.height());
        assertTrue(pattern.matches(grid(2, 1, new ItemStack(Items.COAL, 64), new ItemStack(Items.IRON_INGOT))));
        assertTrue(pattern.matches(grid(2, 1, new ItemStack(Items.IRON_INGOT), new ItemStack(Items.COAL))));
        assertFalse(pattern.matches(grid(2, 1, new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.COAL))));
        var encoded = ArcaneShapedRecipePattern.MAP_CODEC.codec().encodeStart(JsonOps.INSTANCE, pattern).result().orElseThrow();
        assertEquals(JsonParser.parseString("[\"   \",\" CI\",\"   \"]"), encoded.getAsJsonObject().get("pattern"));
    }

    @Test
    void holesDimensionsAndOccupiedSlotCountsRemainPartOfMatching() {
        var pattern = ArcaneShapedRecipePattern.of(Map.of('C', Ingredient.of(Items.COAL), 'I', Ingredient.of(Items.IRON_INGOT)), "C I");
        assertTrue(pattern.matches(grid(3, 1, new ItemStack(Items.COAL), ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT))));
        assertFalse(pattern.matches(grid(3, 1, new ItemStack(Items.COAL), new ItemStack(Items.COAL), new ItemStack(Items.IRON_INGOT))));
        assertFalse(pattern.matches(grid(2, 1, new ItemStack(Items.COAL), new ItemStack(Items.IRON_INGOT))));
        assertFalse(pattern.matches(grid(1, 1, new ItemStack(Items.COAL, 64))));
    }

    @Test
    void malformedKeysAndPatternsStayErrors() {
        for (String json : List.of("{\"key\":{\"CC\":{\"item\":\"minecraft:coal\"}},\"pattern\":[\"C\"]}",
                "{\"key\":{\" \":{\"item\":\"minecraft:coal\"}},\"pattern\":[\" \"]}",
                "{\"key\":{},\"pattern\":[\"C\"]}", "{\"key\":{\"C\":{\"item\":\"minecraft:coal\"}},\"pattern\":[\"I\"]}",
                "{\"key\":{},\"pattern\":[]}", "{\"key\":{},\"pattern\":[\"C\",\"CC\"]}",
                "{\"key\":{},\"pattern\":[\"CCCC\"]}")) {
            var result = ArcaneShapedRecipePattern.MAP_CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json));
            assertTrue(result.error().isPresent(), json);
            assertTrue(result.result().isEmpty(), json);
        }
    }

    @Test
    void strictKeysRejectPartialIngredientListsInsteadOfLoadingPartialRecipes() {
        var result = ArcaneShapedRecipePattern.MAP_CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(
                "{\"key\":{\"C\":[{\"item\":\"minecraft:coal\"},{\"item\":\"thaumaturge:missing\"}]},\"pattern\":[\"C\"]}"));
        assertTrue(result.error().isPresent());
        assertTrue(result.result().isEmpty());
        assertTrue(result.error().orElseThrow().message().contains("Map entry 'C'"));
    }

    @Test
    void originalPatternWireCodecPreservesLayoutAndMatchingButHasNoJsonSourceData() {
        var source = ArcaneShapedRecipePattern.of(Map.of('C', Ingredient.of(Items.COAL), 'I', Ingredient.of(Items.IRON_INGOT)), "C I");
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        try {
            ArcaneShapedRecipePattern.STREAM_CODEC.encode(buffer, source);
            var decoded = ArcaneShapedRecipePattern.STREAM_CODEC.decode(buffer);
            assertEquals(3, decoded.width());
            assertEquals(1, decoded.height());
            assertTrue(decoded.ingredients().get(1).isEmpty());
            assertTrue(decoded.matches(grid(3, 1, new ItemStack(Items.COAL), ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT))));
            assertTrue(ArcaneShapedRecipePattern.MAP_CODEC.codec().encodeStart(JsonOps.INSTANCE, decoded).error().isPresent());
            assertEquals(0, buffer.readableBytes());
            assertEquals(1, buffer.refCnt());
        } finally { buffer.release(); }
    }

    @Test
    void configuredGridExpansionIsMonotonicAndSupportsLargerPatterns() {
        ArcaneShapedRecipePattern.setCraftingSize(4, 4);
        ArcaneShapedRecipePattern.setCraftingSize(2, 2);
        assertEquals(4, ArcaneShapedRecipePattern.getMaxWidth());
        assertEquals(4, ArcaneShapedRecipePattern.getMaxHeight());
        var pattern = parse("{\"key\":{\"C\":{\"item\":\"minecraft:coal\"}},\"pattern\":[\"CCCC\"]}");
        assertTrue(pattern.matches(grid(4, 1, new ItemStack(Items.COAL), new ItemStack(Items.COAL),
                new ItemStack(Items.COAL), new ItemStack(Items.COAL))));
    }

    @Test
    void allSpacePatternsAreReportedAsErrorsRatherThanCrashing() {
        var parsed = ArcaneShapedRecipePattern.MAP_CODEC.codec().parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"key\":{},\"pattern\":[\"   \",\"   \"]}"));
        assertTrue(parsed.error().isPresent());
        assertTrue(parsed.result().isEmpty());
        assertThrows(IllegalStateException.class, () -> ArcaneShapedRecipePattern.of(Map.of(), "   "));
    }
}
