package com.leclowndu93150.thaumaturge.recipe;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

public class RecipeAccessRuntimeTest {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("thaumaturge", "lookup_test");

    private SmeltingRecipe recipe(ResourceLocation id, Item ingredient, Item result) {
        return new SmeltingRecipe(id, "test", CookingBookCategory.MISC, Ingredient.of(ingredient),
                new ItemStack(result), 1F, 20);
    }

    private Level level() {
        Level level = mock(Level.class);
        when(level.registryAccess()).thenReturn(RegistryAccess.EMPTY);
        return level;
    }

    @Test
    void holderEqualityAndHashingUseOnlyThePinnedBaselinesIdentifier() {
        var first = new RecipeHolder<>(ID, recipe(ID, Items.COAL, Items.DIAMOND));
        var second = new RecipeHolder<>(ID, recipe(ID, Items.IRON_ORE, Items.EMERALD));
        assertEquals(first, second);
        assertEquals(ID.hashCode(), first.hashCode());
        assertEquals(ID.toString(), first.toString());
        assertNotEquals(first, new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath("thaumaturge", "other"), first.value()));
        assertEquals(1, Set.of(first).stream().filter(second::equals).count());
    }

    @Test
    void nativeCatalogQueriesShareTheSameHolderInstanceAndActualValue() {
        RecipeManager manager = new RecipeManager();
        SmeltingRecipe recipe = recipe(ID, Items.COAL, Items.DIAMOND);
        manager.replaceRecipes(List.of(recipe));
        RecipeAccess access = RecipeAccess.of(manager);
        assertSame(access, RecipeAccess.of(manager));
        assertNotSame(access, RecipeAccess.of(new RecipeManager()));
        RecipeHolder<?> holder = access.byKey(ID).orElseThrow();
        assertSame(recipe, holder.value());
        assertEquals(ID, holder.id());
        assertSame(holder, access.byKey(ID).orElseThrow());
        assertSame(holder, access.getAllRecipesFor(RecipeType.SMELTING).get(0));
        assertSame(holder, access.getRecipes().get(0));
        assertEquals(Set.of(ID), access.getRecipeIds().collect(Collectors.toSet()));
        assertTrue(access.byKey(ResourceLocation.fromNamespaceAndPath("thaumaturge", "missing")).isEmpty());
    }

    @Test
    void nativeMatchingAndHintsReturnStableHoldersWithoutChangingInputs() {
        RecipeManager manager = new RecipeManager();
        manager.replaceRecipes(List.of(recipe(ID, Items.COAL, Items.DIAMOND)));
        RecipeAccess access = RecipeAccess.of(manager);
        SimpleContainer input = new SimpleContainer(new ItemStack(Items.COAL, 3));
        RecipeHolder<?> holder = access.byKey(ID).orElseThrow();
        assertSame(holder, access.getRecipeFor(RecipeType.SMELTING, input, level()).orElseThrow());
        assertSame(holder, access.getRecipeFor(RecipeType.SMELTING, input, level(), ID).orElseThrow());
        assertSame(holder, access.getRecipeFor(RecipeType.SMELTING, input, level(),
                ResourceLocation.fromNamespaceAndPath("thaumaturge", "missing_hint")).orElseThrow());
        assertSame(holder, access.getRecipesFor(RecipeType.SMELTING, input, level()).get(0));
        assertEquals(3, input.getItem(0).getCount());
        assertTrue(access.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(new ItemStack(Items.DIRT)), level()).isEmpty());
    }

    @Test
    void nativeCraftingRemaindersKeepTheBucketWithoutConsumingTheInput() {
        RecipeManager manager = new RecipeManager();
        manager.replaceRecipes(List.of(new ShapelessRecipe(ID, "test", CraftingBookCategory.MISC,
                new ItemStack(Items.STONE), NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.LAVA_BUCKET)))));
        var input = new TransientCraftingContainer(mock(AbstractContainerMenu.class), 1, 1);
        input.setItem(0, new ItemStack(Items.LAVA_BUCKET));
        var remainders = RecipeAccess.of(manager).getRemainingItemsFor(RecipeType.CRAFTING, input, level());
        assertEquals(1, remainders.size());
        assertTrue(remainders.get(0).is(Items.BUCKET));
        assertTrue(input.getItem(0).is(Items.LAVA_BUCKET));
    }

    @Test
    void replacingEvenTheSameNativeRecipeInvalidatesTheHolderAndKeepsOldReferencesValid() {
        RecipeManager manager = new RecipeManager();
        SmeltingRecipe recipe = recipe(ID, Items.COAL, Items.DIAMOND);
        manager.replaceRecipes(List.of(recipe));
        RecipeAccess access = RecipeAccess.of(manager);
        RecipeHolder<?> before = access.byKey(ID).orElseThrow();
        manager.replaceRecipes(List.of(recipe));
        RecipeHolder<?> after = access.byKey(ID).orElseThrow();
        assertNotSame(before, after);
        assertSame(recipe, before.value());
        assertSame(recipe, after.value());
        assertEquals(before, after);
        manager.replaceRecipes(List.of());
        assertTrue(access.getRecipes().isEmpty());
        assertTrue(access.byKey(ID).isEmpty());
        assertSame(recipe, before.value());
    }

    @Test
    void actualJsonApplyReplacesCatalogEntriesAndRefreshesTheView() throws Exception {
        RecipeManager manager = new RecipeManager();
        manager.replaceRecipes(List.of(recipe(ID, Items.COAL, Items.DIAMOND)));
        RecipeAccess access = RecipeAccess.of(manager);
        RecipeHolder<?> before = access.byKey(ID).orElseThrow();
        var json = JsonParser.parseString("""
                {"type":"minecraft:smelting","ingredient":{"item":"minecraft:coal"},
                 "result":"minecraft:emerald","experience":0.5,"cookingtime":50}
                """);
        var apply = RecipeManager.class.getDeclaredMethod("apply", Map.class, ResourceManager.class, ProfilerFiller.class);
        apply.setAccessible(true);
        apply.invoke(manager, Map.of(ID, json), mock(ResourceManager.class), InactiveProfiler.INSTANCE);
        RecipeHolder<?> after = access.byKey(ID).orElseThrow();
        assertNotSame(before, after);
        assertNotSame(before.value(), after.value());
        assertTrue(after.value().getResultItem(RegistryAccess.EMPTY).is(Items.EMERALD));
        assertTrue(before.value().getResultItem(RegistryAccess.EMPTY).is(Items.DIAMOND));
        assertSame(after, access.getRecipes().get(0));
        assertFalse(manager.hadErrorsLoading());
    }

    @Test
    void holderStreamUsesTheNativeSerializerAndRestoresTheExternalId() {
        RecipeHolder<?> holder = new RecipeHolder<>(ID, recipe(ID, Items.COAL, Items.DIAMOND));
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            RecipeHolder.STREAM_CODEC.encode(buffer, holder);
            RecipeHolder<?> decoded = RecipeHolder.STREAM_CODEC.decode(buffer);
            assertEquals(ID, decoded.id());
            assertEquals(ID, decoded.value().getId());
            assertInstanceOf(SmeltingRecipe.class, decoded.value());
            assertTrue(decoded.value().getResultItem(RegistryAccess.EMPTY).is(Items.DIAMOND));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void unknownSerializerAndTruncatedStreamsFailInsteadOfProducingARecipe() {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            buffer.writeResourceLocation(ID);
            buffer.writeVarInt(Integer.MAX_VALUE);
            assertThrows(DecoderException.class, () -> RecipeHolder.STREAM_CODEC.decode(buffer));
            buffer.clear();
            buffer.writeResourceLocation(ID);
            assertThrows(IndexOutOfBoundsException.class, () -> RecipeHolder.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void anUnregisteredSerializerCannotBeWritten() {
        Recipe<?> value = mock(Recipe.class);
        doReturn(mock(RecipeSerializer.class)).when(value).getSerializer();
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            assertThrows(EncoderException.class, () -> RecipeHolder.STREAM_CODEC.encode(buffer, new RecipeHolder<>(ID, value)));
        } finally {
            buffer.release();
        }
    }
}
