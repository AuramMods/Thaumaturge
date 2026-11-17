package com.leclowndu93150.thaumaturge.recipe;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionInput;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

public class RecipeInputRuntimeTest {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("thaumaturge", "input_test");

    private List<ItemStack> emptyGrid() { return new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY)); }

    private static final class Fixture extends CustomRecipe {
        CraftingInput seenInput;
        HolderLookup.Provider seenRegistries;

        Fixture() { super(CraftingBookCategory.MISC); }
        @Override public boolean matches(CraftingInput input, Level level) {
            seenInput = input;
            return input.ingredientCount() == 1 && input.getItem(0).is(Items.LAVA_BUCKET);
        }
        @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
            seenInput = input;
            seenRegistries = registries;
            return new ItemStack(Items.STONE);
        }
        @Override public ItemStack getResultItem(HolderLookup.Provider registries) {
            seenRegistries = registries;
            return new ItemStack(Items.STONE);
        }
        @Override public boolean canCraftInDimensions(int width, int height) { return width > 0 && height > 0; }
        @Override public RecipeSerializer<?> getSerializer() { throw new UnsupportedOperationException("Fixture has no wire serializer"); }
    }

    @Test
    void singleInputRetainsTheNativeIndexContractAndStackIdentity() {
        ItemStack stack = new ItemStack(Items.COAL, 32);
        var input = new SingleRecipeInput(stack);
        Container nativeInput = input;
        assertEquals(1, nativeInput.getContainerSize());
        assertSame(stack, nativeInput.getItem(0));
        assertFalse(nativeInput.isEmpty());
        assertTrue(new SingleRecipeInput(ItemStack.EMPTY).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> input.getItem(-1));
        assertThrows(IllegalArgumentException.class, () -> input.getItem(1));
    }

    @Test
    void recipeViewsRejectInventoryMutationInsteadOfSilentlyIgnoringIt() {
        ItemStack stack = new ItemStack(Items.COAL, 4);
        RecipeInput input = new SingleRecipeInput(stack);
        assertThrows(UnsupportedOperationException.class, () -> input.setItem(0, ItemStack.EMPTY));
        assertThrows(UnsupportedOperationException.class, () -> input.removeItem(0, 1));
        assertThrows(UnsupportedOperationException.class, () -> input.removeItemNoUpdate(0));
        assertThrows(UnsupportedOperationException.class, input::clearContent);
        assertThrows(UnsupportedOperationException.class, input::setChanged);
        assertThrows(UnsupportedOperationException.class, () -> input.stillValid(null));
        assertEquals(4, input.getItem(0).getCount());
    }

    @Test
    void actualDustAndInfusionInputsExposeTheirExistingSlotLayouts() {
        ItemStack catalyst = new ItemStack(Items.DIAMOND), component = new ItemStack(Items.COAL);
        var dust = new DustTriggerInput(catalyst, mock(Level.class), BlockPos.ZERO, Blocks.STONE.defaultBlockState());
        assertEquals(1, dust.getContainerSize());
        assertSame(catalyst, dust.getItem(0));
        assertTrue(dust.getItem(-1).isEmpty()); // The mod's own dust input deliberately differs from SingleRecipeInput.
        var infusion = new InfusionInput(catalyst, List.of(component));
        assertEquals(2, infusion.getContainerSize());
        assertSame(catalyst, infusion.getItem(0));
        assertSame(component, infusion.getItem(1));
        assertFalse(infusion.isEmpty());
    }

    @Test
    void trimmingKeepsOffsetsInteriorHolesAndOriginalStackReferences() {
        List<ItemStack> grid = emptyGrid();
        ItemStack first = new ItemStack(Items.COAL, 4), last = new ItemStack(Items.IRON_ORE, 2);
        grid.set(4, first);
        grid.set(8, last);
        var positioned = CraftingInput.ofPositioned(3, 3, grid);
        CraftingInput input = positioned.input();
        assertEquals(1, positioned.left());
        assertEquals(1, positioned.top());
        assertEquals(2, input.width());
        assertEquals(2, input.height());
        assertEquals(4, input.getContainerSize());
        assertEquals(2, input.ingredientCount());
        assertSame(first, input.getItem(0, 0));
        assertSame(last, input.getItem(1, 1));
        assertTrue(input.getItem(1, 0).isEmpty());
        assertSame(input, CraftingInput.from(input));
        assertEquals(4, first.getCount());
    }

    @Test
    void fullBoundsRetainTheBackingListAsInThePinnedBaseline() {
        List<ItemStack> grid = emptyGrid();
        grid.set(0, new ItemStack(Items.COAL));
        grid.set(8, new ItemStack(Items.COAL));
        CraftingInput input = CraftingInput.of(3, 3, grid);
        assertSame(grid, input.items());
        assertSame(grid, input.getItems());
        assertEquals(3, input.getWidth());
        assertEquals(3, input.getHeight());
    }

    @Test
    void emptyAndZeroSizedInputsRetainBaselineBoundaryBehavior() {
        assertSame(CraftingInput.EMPTY, CraftingInput.of(0, 3, List.of()));
        assertSame(CraftingInput.EMPTY, CraftingInput.of(3, 3, emptyGrid()));
        CraftingInput oneEmpty = CraftingInput.of(1, 1, List.of(ItemStack.EMPTY));
        assertEquals(1, oneEmpty.size());
        assertTrue(oneEmpty.isEmpty());
        assertEquals(0, oneEmpty.ingredientCount());
    }

    @Test
    void nativeShapedMatchingWorksAgainstTheTrimmedContainerView() {
        var recipe = new ShapedRecipe(ID, "test", CraftingBookCategory.MISC, 1, 2,
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.COAL), Ingredient.of(Items.IRON_ORE)), new ItemStack(Items.STONE));
        List<ItemStack> grid = emptyGrid();
        grid.set(4, new ItemStack(Items.COAL));
        grid.set(7, new ItemStack(Items.IRON_ORE));
        var input = CraftingInput.of(3, 3, grid);
        assertTrue(recipe.matches(input, mock(Level.class)));
        assertTrue(recipe.assemble(input, RegistryAccess.EMPTY).is(Items.STONE));
        grid.set(7, new ItemStack(Items.DIRT));
        assertFalse(recipe.matches(CraftingInput.of(3, 3, grid), mock(Level.class)));
    }

    @Test
    void nativeShapelessMatchingCountsOccupiedSlotsRatherThanStackAmounts() {
        var recipe = new ShapelessRecipe(ID, "test", CraftingBookCategory.MISC, new ItemStack(Items.STONE),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.COAL), Ingredient.of(Items.COAL)));
        var one = CraftingInput.of(1, 1, List.of(new ItemStack(Items.COAL, 64)));
        var two = CraftingInput.of(2, 1, List.of(new ItemStack(Items.COAL, 64), new ItemStack(Items.COAL, 64)));
        assertFalse(recipe.matches(one, mock(Level.class)));
        assertFalse(one.stackedContents().canCraft(recipe, null));
        assertTrue(recipe.matches(two, mock(Level.class)));
        assertTrue(two.stackedContents().canCraft(recipe, null));
        assertEquals(2, two.ingredientCount());
    }

    @Test
    void nativeStackedContentsFillingStillCountsAvailableItems() {
        var recipe = new ShapelessRecipe(ID, "test", CraftingBookCategory.MISC, new ItemStack(Items.STONE),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.COAL)));
        var input = CraftingInput.of(1, 1, List.of(new ItemStack(Items.COAL, 32)));
        StackedContents contents = new StackedContents();
        input.fillStackedContents(contents);
        assertTrue(contents.canCraft(recipe, null, 32));
        assertFalse(input.stackedContents().canCraft(recipe, null, 32));
    }

    @Test
    void gridEqualityAndHashRetainItemDataAndCountDistinctions() {
        ItemStack first = new ItemStack(Items.COAL, 3);
        first.setHoverName(Component.literal("named"));
        var a = CraftingInput.of(1, 1, List.of(first));
        var copy = CraftingInput.of(1, 1, List.of(first.copy()));
        assertEquals(a, copy);
        assertEquals(a.hashCode(), copy.hashCode());
        assertNotEquals(a, CraftingInput.of(1, 1, List.of(first.copyWithCount(2))));
        assertNotEquals(a, CraftingInput.of(1, 1, List.of(new ItemStack(Items.COAL, 3))));
    }

    @Test
    void emptyStackDataDoesNotViolateTheListHashContract() {
        ItemStack emptied = new ItemStack(Items.COAL);
        emptied.setHoverName(Component.literal("discarded"));
        emptied.setCount(0);
        assertTrue(ItemData.listMatches(List.of(ItemStack.EMPTY), List.of(emptied)));
        assertEquals(ItemData.hashStackList(List.of(ItemStack.EMPTY)), ItemData.hashStackList(List.of(emptied)));
    }

    @Test
    void nativeSpecialRecipeCallbacksReceiveTrimmedInputsAndExplicitRegistries() {
        Fixture recipe = new Fixture();
        CraftingContainer input = new TransientCraftingContainer(mock(AbstractContainerMenu.class), 3, 3);
        input.setItem(4, new ItemStack(Items.LAVA_BUCKET));
        Recipe<CraftingContainer> nativeRecipe = recipe;
        assertTrue(nativeRecipe.matches(input, mock(Level.class)));
        assertEquals(1, recipe.seenInput.width());
        RegistryAccess first = mock(RegistryAccess.class), second = mock(RegistryAccess.class);
        assertTrue(nativeRecipe.assemble(input, first).is(Items.STONE));
        assertSame(first, recipe.seenRegistries);
        assertTrue(nativeRecipe.getResultItem(second).is(Items.STONE));
        assertSame(second, recipe.seenRegistries);
        assertTrue(recipe.isSpecial());
        assertSame(RecipeType.CRAFTING, recipe.getType());
    }

    @Test
    void nativeRecipeManagerGetsRemaindersExpandedBackToTheOriginalSlot() {
        Fixture recipe = new Fixture();
        recipe.bindRecipeId(ID);
        RecipeManager manager = new RecipeManager();
        manager.replaceRecipes(List.of(recipe));
        var input = new TransientCraftingContainer(mock(AbstractContainerMenu.class), 3, 3);
        input.setItem(4, new ItemStack(Items.LAVA_BUCKET));
        var remaining = manager.getRemainingItemsFor(RecipeType.CRAFTING, input, mock(Level.class));
        assertEquals(9, remaining.size());
        for (int i = 0; i < 9; i++) assertTrue(i == 4 ? remaining.get(i).is(Items.BUCKET) : remaining.get(i).isEmpty());
        assertTrue(input.getItem(4).is(Items.LAVA_BUCKET));
        assertSame(recipe, manager.byKey(ID).orElseThrow());
    }

    @Test
    void identifiersAreRealStableAndCannotBeReboundAcrossResources() {
        Fixture recipe = new Fixture();
        assertThrows(IllegalStateException.class, recipe::getId);
        recipe.bindRecipeId(ID);
        recipe.bindRecipeId(ID);
        assertEquals(ID, recipe.getId());
        assertThrows(IllegalStateException.class,
                () -> recipe.bindRecipeId(ResourceLocation.fromNamespaceAndPath("thaumaturge", "another")));
        assertEquals(ID, recipe.getId());
    }

    @Test
    void statelessCodecsCreateIndependentValuesForDifferentResourceIds() {
        MapCodec<Fixture> json = MapCodec.unit(Fixture::new);
        Fixture a = json.codec().parse(JsonOps.INSTANCE, new JsonObject()).result().orElseThrow();
        Fixture b = json.codec().parse(JsonOps.INSTANCE, new JsonObject()).result().orElseThrow();
        a.bindRecipeId(ID);
        b.bindRecipeId(ResourceLocation.fromNamespaceAndPath("thaumaturge", "another"));
        assertNotSame(a, b);
        assertNotEquals(a.getId(), b.getId());
        StreamCodec<ByteBuf, Fixture> stream = StreamCodec.unitFactory(Fixture::new);
        ByteBuf buffer = Unpooled.buffer();
        try {
            stream.encode(buffer, a);
            assertEquals(0, buffer.readableBytes());
            assertNotSame(stream.decode(buffer), stream.decode(buffer));
        } finally { buffer.release(); }
    }
}
