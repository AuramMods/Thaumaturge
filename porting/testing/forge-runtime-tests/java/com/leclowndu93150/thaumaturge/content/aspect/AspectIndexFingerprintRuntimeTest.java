package com.leclowndu93150.thaumaturge.content.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.junit.jupiter.api.Test;

class AspectIndexFingerprintRuntimeTest {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("port_aspects", "smelting");
    private static final ResourceLocation OTHER = ID.withPath("other");
    private static final String SECTION = "recipes/port_aspects";
    private static final TagKey<Item> ORES = TagKey.create(Registries.ITEM, ID.withPath("source_ores"));

    @Test
    void authoredRecipeFieldsInvalidateButJsonObjectOrderIsStable() throws Exception {
        RecipeManager manager = new RecipeManager();
        MinecraftServer server = server(manager);
        apply(manager, """
                {"type":"minecraft:smelting","ingredient":{"item":"minecraft:iron_ore"},
                 "result":"minecraft:iron_ingot","index_metadata":{"source":"old","weight":2}}
                """);
        AspectIndexFingerprint first = AspectIndexFingerprint.compute(server);
        assertTrue(first.cacheable());

        apply(manager, """
                {"index_metadata":{"weight":2,"source":"old"},"result":"minecraft:iron_ingot",
                 "ingredient":{"item":"minecraft:iron_ore"},"type":"minecraft:smelting"}
                """);
        assertEquals(first, AspectIndexFingerprint.compute(server));

        apply(manager, """
                {"type":"minecraft:smelting","ingredient":{"item":"minecraft:iron_ore"},
                 "result":"minecraft:iron_ingot","index_metadata":{"source":"new","weight":2}}
                """);
        AspectIndexFingerprint changed = AspectIndexFingerprint.compute(server);
        assertDifferentRecipeSection(first, changed);
    }

    @Test
    void programmaticRecipesFingerprintTheirNativeOrderIndependentContents() {
        RecipeManager manager = new RecipeManager();
        MinecraftServer server = server(manager);
        var iron = smelting(ID, Items.IRON_ORE, Items.IRON_INGOT);
        var gold = smelting(OTHER, Items.GOLD_ORE, Items.GOLD_INGOT);
        manager.replaceRecipes(List.of(iron, gold));
        AspectIndexFingerprint first = AspectIndexFingerprint.compute(server);
        manager.replaceRecipes(List.of(gold, iron));
        assertEquals(first, AspectIndexFingerprint.compute(server));

        manager.replaceRecipes(List.of(gold, smelting(ID, Items.IRON_ORE, Items.DIAMOND)));
        assertDifferentRecipeSection(first, AspectIndexFingerprint.compute(server));

        manager.replaceRecipes(List.of(gold));
        assertDifferentRecipeSection(first, AspectIndexFingerprint.compute(server));
    }

    @Test
    void sameAuthoredTagRecipeChangesWhenItsResolvedIngredientsChange() throws Exception {
        var items = BuiltInRegistries.ITEM;
        var previous = new LinkedHashMap<TagKey<Item>, List<Holder<Item>>>();
        items.getTags().forEach(pair -> previous.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var tags = new LinkedHashMap<>(previous);
        RecipeManager manager = new RecipeManager();
        MinecraftServer server = server(manager);
        String recipe = """
                {"type":"minecraft:smelting","ingredient":{"tag":"port_aspects:source_ores"},
                 "result":"minecraft:iron_ingot"}
                """;

        try {
            tags.put(ORES, List.of(Items.IRON_ORE.builtInRegistryHolder()));
            items.bindTags(tags);
            Ingredient.invalidateAll();
            apply(manager, recipe);
            AspectIndexFingerprint first = AspectIndexFingerprint.compute(server);

            tags.put(ORES, List.of(Items.DEEPSLATE_IRON_ORE.builtInRegistryHolder()));
            items.bindTags(tags);
            Ingredient.invalidateAll();
            apply(manager, recipe);
            assertDifferentRecipeSection(first, AspectIndexFingerprint.compute(server));
        } finally {
            items.bindTags(previous);
            Ingredient.invalidateAll();
        }
    }

    @Test
    void anUnencodableNativeRecipeDisablesPersistentCacheReuse() {
        RecipeManager manager = new RecipeManager();
        MinecraftServer server = server(manager);
        Recipe<?> unsupported = mock(Recipe.class);
        when(unsupported.getId()).thenReturn(OTHER);
        doReturn(RecipeType.SMELTING).when(unsupported).getType();
        when(unsupported.getSerializer()).thenThrow(new IllegalStateException("missing test serializer"));
        var valid = smelting(ID, Items.IRON_ORE, Items.IRON_INGOT);
        manager.replaceRecipes(List.of(unsupported, valid));
        var fingerprint = AspectIndexFingerprint.compute(server);
        assertFalse(fingerprint.cacheable());
        assertNotNull(fingerprint.sections().get(SECTION));

        manager.replaceRecipes(List.of(valid));
        assertTrue(AspectIndexFingerprint.compute(server).cacheable());
    }

    private static void assertDifferentRecipeSection(AspectIndexFingerprint expected, AspectIndexFingerprint changed) {
        assertTrue(expected.cacheable());
        assertTrue(changed.cacheable());
        assertNotEquals(expected.digest(), changed.digest());
        assertEquals(SECTION, changed.describeChanges(expected.sectionsJson()));
    }

    private static MinecraftServer server(RecipeManager manager) {
        MinecraftServer server = mock(MinecraftServer.class);
        when(server.getRecipeManager()).thenReturn(manager);
        when(server.registryAccess()).thenReturn(RegistryAccess.EMPTY);
        return server;
    }

    private static SmeltingRecipe smelting(ResourceLocation id, Item input, Item output) {
        return new SmeltingRecipe(id, "", CookingBookCategory.MISC, Ingredient.of(input),
                new ItemStack(output), 0.35F, 200);
    }

    private static void apply(RecipeManager manager, String source) throws Exception {
        var method = RecipeManager.class.getDeclaredMethod("apply", Map.class, ResourceManager.class,
                ProfilerFiller.class);
        method.setAccessible(true);
        method.invoke(manager, Map.of(ID, JsonParser.parseString(source)), mock(ResourceManager.class),
                InactiveProfiler.INSTANCE);
        assertFalse(manager.hadErrorsLoading());
    }
}
