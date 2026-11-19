package com.leclowndu93150.thaumaturge.recipe;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.data.recipe.RecipeDataOutput;
import com.leclowndu93150.thaumaturge.data.recipe.builders.SimpleRecipeBuilder;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RecipeSerializerRuntimeTest {
    private static final ResourceLocation TYPE_ID = ResourceLocation.fromNamespaceAndPath("thaumaturge", "serde_fixture");
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("thaumaturge", "serialized");
    private static final ResourceKey<Registry<String>> VALUES = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "fixture_values"));
    private static final ResourceKey<String> VALUE = ResourceKey.create(VALUES, ResourceLocation.withDefaultNamespace("value"));
    private static final ResourceKey<String> OTHER = ResourceKey.create(VALUES, ResourceLocation.withDefaultNamespace("other"));
    private static final RecipeType<ValueRecipe> TYPE = new RecipeType<>() {};
    private static final MapCodec<ValueRecipe> CODEC = RegistryFixedCodec.create(VALUES).fieldOf("value")
            .xmap(ValueRecipe::new, recipe -> recipe.value);
    private static final StreamCodec<RegistryFriendlyByteBuf, ValueRecipe> STREAM =
            ByteBufCodecs.holderRegistry(VALUES).map(ValueRecipe::new, recipe -> recipe.value);
    private static final SimpleRecipeSerializer<ValueRecipe> SERIALIZER = new SimpleRecipeSerializer<>(CODEC, STREAM);

    public static final class ValueRecipe extends AbstractRecipe<SingleRecipeInput> {
        final Holder<String> value;
        ValueRecipe(Holder<String> value) { this.value = value; }
        @Override public boolean matches(SingleRecipeInput input, Level level) { return true; }
        @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return ItemStack.EMPTY; }
        @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
        @Override public boolean canCraftInDimensions(int width, int height) { return true; }
        @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
        @Override public RecipeType<?> getType() { return TYPE; }
    }

    @BeforeAll
    static void registerTheActualFixtureTypeAndSerializer() {
        ForgeRegistries.RECIPE_TYPES.register(TYPE_ID, TYPE);
        ForgeRegistries.RECIPE_SERIALIZERS.register(TYPE_ID, SERIALIZER);
    }

    private RegistryAccess.Frozen registries(String prefix, boolean reversed) {
        MappedRegistry<String> registry = new MappedRegistry<>(VALUES, Lifecycle.stable());
        if (reversed) registry.register(OTHER, prefix + "_other", Lifecycle.stable());
        registry.register(VALUE, prefix + "_value", Lifecycle.stable());
        if (!reversed) registry.register(OTHER, prefix + "_other", Lifecycle.stable());
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
    }

    private JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE_ID.toString());
        json.addProperty("value", VALUE.location().toString());
        return json;
    }

    private void apply(RecipeManager manager, JsonObject json) throws Exception {
        var apply = RecipeManager.class.getDeclaredMethod("apply", Map.class, ResourceManager.class, ProfilerFiller.class);
        apply.setAccessible(true);
        apply.invoke(manager, Map.of(ID, json), mock(ResourceManager.class), InactiveProfiler.INSTANCE);
    }

    @Test
    void nativeDatagenWritesRegistryAwareRecipesAndMatchingConditionalUnlocks(@TempDir Path output) throws Exception {
        var registries = registries("datagen", false);
        var tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("thaumaturge", "optional_ore_fixture"));
        var condition = new NotCondition(new TagEmptyCondition(tag.location()));
        var recipe = new ValueRecipe(registries.registryOrThrow(VALUES).getHolderOrThrow(VALUE));
        var optional = ResourceLocation.fromNamespaceAndPath("thaumaturge", "optional/serialized");
        var hidden = ResourceLocation.fromNamespaceAndPath("thaumaturge", "hidden/serialized");
        var provider = new RecipeProvider(new PackOutput(output)) {
            @Override
            protected void buildRecipes(Consumer<FinishedRecipe> recipes) {
                var builder = new SimpleRecipeBuilder(new ItemStack(Items.STONE), RecipeCategory.MISC) {
                    @Override
                    public void save(Consumer<FinishedRecipe> sink, ResourceLocation id) {
                        RecipeDataOutput.saveCustom(sink, id, recipe, buildAdvancement(id));
                    }
                };
                builder.save(recipes, hidden);
                builder.unlockedBy("has_stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE));
                builder.unlockedBy("has_flint", InventoryChangeTrigger.TriggerInstance.hasItems(Items.FLINT));
                builder.save(RecipeDataOutput.withCondition(recipes, condition), optional);
            }
        };
        RegistryContext.withRegistries(registries, () -> provider.run(CachedOutput.NO_CACHE)).join();
        assertNull(RegistryContext.scoped());

        var recipePath = output.resolve("data/thaumaturge/recipes");
        var hiddenJson = JsonParser.parseString(Files.readString(recipePath.resolve("hidden/serialized.json")))
                .getAsJsonObject();
        assertEquals(json(), hiddenJson);
        assertFalse(Files.exists(output.resolve("data/thaumaturge/advancements/recipes/misc/hidden/serialized.json")));

        var optionalJson = JsonParser.parseString(Files.readString(recipePath.resolve("optional/serialized.json")))
                .getAsJsonObject();
        assertEquals("forge:conditional", optionalJson.get("type").getAsString());
        assertNull(RecipeManager.fromJson(optional, optionalJson, ICondition.IContext.EMPTY));
        var populatedTags = mock(ICondition.IContext.class);
        when(populatedTags.getTag(tag)).thenReturn(List.of(Items.STONE.builtInRegistryHolder()));
        var loaded = (ValueRecipe) RegistryContext.withRegistries(
                registries, () -> RecipeManager.fromJson(optional, optionalJson, populatedTags));
        assertEquals(optional, loaded.getId());
        assertSame(recipe.value, loaded.value);

        var unlock = optional.withPrefix("recipes/misc/");
        var unlockJson = JsonParser.parseString(Files.readString(
                output.resolve("data/thaumaturge/advancements/" + unlock.getPath() + ".json"))).getAsJsonObject();
        var context = new DeserializationContext(unlock, new LootDataManager());
        assertNull(Advancement.Builder.fromJson(unlockJson, context, ICondition.IContext.EMPTY));
        var loadedUnlock = Advancement.Builder.fromJson(unlockJson, context, populatedTags).serializeToJson();
        assertEquals("minecraft:recipes/root", loadedUnlock.get("parent").getAsString());
        assertEquals(optional.toString(), loadedUnlock.getAsJsonObject("rewards").getAsJsonArray("recipes")
                .get(0).getAsString());
        var requirements = loadedUnlock.getAsJsonArray("requirements");
        assertEquals(1, requirements.size());
        assertEquals(3, requirements.get(0).getAsJsonArray().size());
        assertEquals(3, loadedUnlock.getAsJsonObject("criteria").size());
    }

    @Test
    void actualJsonCallbackUsesTheScopeAndBindsIndependentResourceIds() {
        var registries = registries("a", false);
        ValueRecipe first = RegistryContext.withRegistries(registries, () -> SERIALIZER.fromJson(ID, json()));
        ResourceLocation otherId = ResourceLocation.fromNamespaceAndPath("thaumaturge", "another");
        ValueRecipe second = RegistryContext.withRegistries(registries, () -> SERIALIZER.fromJson(otherId, json()));
        assertEquals(ID, first.getId());
        assertEquals(otherId, second.getId());
        assertNotSame(first, second);
        assertSame(registries.registryOrThrow(VALUES).getHolderOrThrow(VALUE), first.value);
        assertEquals("a_value", first.value.value());
        assertNull(RegistryContext.scoped());
    }

    @Test
    void actualManagerJsonApplyUsesItsOwnerContextAndRestoresTheOuterScope() throws Exception {
        var outer = registries("outer", false);
        var owner = registries("owner", true);
        RecipeManager manager = new RecipeManager();
        RecipeAccess access = RecipeAccess.of(manager);
        access.attachRegistries(owner);
        RegistryContext.withRegistries(outer, () -> {
            try { apply(manager, json()); }
            catch (Exception failure) { throw new AssertionError(failure); }
            assertSame(outer, RegistryContext.scoped());
            return null;
        });
        var recipe = (ValueRecipe) manager.byKey(ID).orElseThrow();
        assertEquals("owner_value", recipe.value.value());
        assertEquals(ID, recipe.getId());
        assertFalse(manager.hadErrorsLoading());
        manager.replaceRecipes(List.of(recipe));
        assertSame(owner, access.withRegistries(RegistryContext::scoped));
        assertNull(RegistryContext.scoped());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RegistryAccess.Frozen constructorRegistries(RegistryAccess values) {
        List<Registry<?>> registries = new ArrayList<>();
        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registries().forEach(entry -> registries.add(entry.value()));
        for (RegistryDataLoader.RegistryData<?> data : RegistryDataLoader.WORLDGEN_REGISTRIES) {
            if (registries.stream().noneMatch(registry -> registry.key().equals(data.key()))) {
                registries.add(new MappedRegistry((ResourceKey) data.key(), Lifecycle.stable()).freeze());
            }
        }
        values.registries().forEach(entry -> registries.add(entry.value()));
        return new RegistryAccess.ImmutableRegistryAccess(registries).freeze();
    }

    @Test
    void nativeServerResourceConstructionAttachesTheStartupRegistryLookup() throws Exception {
        var lookup = constructorRegistries(registries("startup", false));
        ReloadableServerResources resources = new ReloadableServerResources(lookup, FeatureFlags.VANILLA_SET,
                Commands.CommandSelection.ALL, 2);
        RecipeAccess access = RecipeAccess.of(resources.getRecipeManager());
        assertSame(lookup, access.withRegistries(RegistryContext::scoped));
        apply(resources.getRecipeManager(), json());
        assertEquals("startup_value", ((ValueRecipe) resources.getRecipeManager().byKey(ID).orElseThrow()).value.value());
    }

    @Test
    void nativeFriendlyBuffersShareTheirIndicesAndUseTheScopedRegistry() {
        var lookup = registries("network", true);
        ValueRecipe value = RegistryContext.withRegistries(lookup, () -> SERIALIZER.fromJson(ID, json()));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            RegistryContext.withRegistries(lookup, () -> { SERIALIZER.toNetwork(buffer, value); return null; });
            assertEquals(1, buffer.readableBytes());
            ValueRecipe decoded = RegistryContext.withRegistries(lookup, () -> SERIALIZER.fromNetwork(ID, buffer));
            assertEquals(ID, decoded.getId());
            assertEquals("network_value", decoded.value.value());
            assertEquals(0, buffer.readableBytes());
            assertEquals(1, buffer.refCnt());
        } finally { buffer.release(); }
    }

    @Test
    void outgoingRecipesKeepTheirLoadOwnerAfterTheLoadingScopeEnds() {
        var owner = registries("loaded", true);
        var unrelated = registries("other_world", false);
        ValueRecipe value = RegistryContext.withRegistries(owner, () -> SERIALIZER.fromJson(ID, json()));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            SERIALIZER.toNetwork(buffer, value);
            assertEquals(1, buffer.readableBytes());
            buffer.clear();
            RegistryContext.withRegistries(unrelated, () -> { SERIALIZER.toNetwork(buffer, value); return null; });
            ValueRecipe decoded = RegistryContext.withRegistries(owner, () -> SERIALIZER.fromNetwork(ID, buffer));
            assertEquals("loaded_value", decoded.value.value());
            assertEquals(ID, decoded.getId());
        } finally { buffer.release(); }
    }

    @Test
    void explicitBufferOwnershipWinsOverAnUnrelatedAmbientRegistry() {
        var outer = registries("outer", false);
        var owner = registries("buffer", true);
        ValueRecipe value = RegistryContext.withRegistries(owner, () -> SERIALIZER.fromJson(ID, json()));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), owner);
        try {
            RegistryContext.withRegistries(outer, () -> {
                SERIALIZER.toNetwork(buffer, value);
                assertEquals("buffer_value", SERIALIZER.fromNetwork(ID, buffer).value.value());
                assertSame(outer, RegistryContext.scoped());
                return null;
            });
        } finally { buffer.release(); }
        assertNull(RegistryContext.scoped());
    }

    @Test
    void lookupOnlyProvidersWorkForRegistryAwareNbtCodecsWithoutNumericIds() {
        var access = registries("lookup", false);
        HolderLookup.Provider lookup = HolderLookup.Provider.create(access.registries().map(entry -> entry.value().asLookup()));
        var serializer = new SimpleRecipeSerializer<>(CODEC, ByteBufCodecs.fromCodecWithRegistries(CODEC.codec()));
        ValueRecipe value = RegistryContext.withRegistries(lookup, () -> serializer.fromJson(ID, json()));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            RegistryContext.withRegistries(lookup, () -> {
                serializer.toNetwork(buffer, value);
                assertEquals("lookup_value", serializer.fromNetwork(ID, buffer).value.value());
                return null;
            });
        } finally { buffer.release(); }
    }

    @Test
    void statelessNativeCallbacksNeedNoWorldAndBindFreshIdentities() {
        var serializer = new SimpleRecipeSerializer<>(MapCodec.unit(() -> new ValueRecipe(Holder.direct("constant"))),
                StreamCodec.unitFactory(() -> new ValueRecipe(Holder.direct("constant"))));
        ValueRecipe a = serializer.fromJson(ID, new JsonObject());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            serializer.toNetwork(buffer, a);
            assertEquals(0, buffer.readableBytes());
            ValueRecipe b = serializer.fromNetwork(ID, buffer);
            assertNotSame(a, b);
            assertEquals(ID, b.getId());
            assertEquals("constant", b.value.value());
        } finally { buffer.release(); }
    }

    @Test
    void malformedJsonAndUnknownWireIdsFailWithoutLeakingRegistryScope() {
        var lookup = registries("failure", false);
        RegistryContext.withRegistries(lookup, () -> {
            JsonParseException failure = assertThrows(JsonParseException.class, () -> SERIALIZER.fromJson(ID, new JsonObject()));
            assertTrue(failure.getMessage().contains(ID.toString()));
            assertSame(lookup, RegistryContext.scoped());
            return null;
        });
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), lookup);
        buffer.writeVarInt(999);
        try { assertThrows(DecoderException.class, () -> SERIALIZER.fromNetwork(ID, buffer)); }
        finally { buffer.release(); }
        assertNull(RegistryContext.scoped());
    }

    @Test
    void concurrentJsonDecodesKeepTheirSeparateLookupOwners() throws Exception {
        var a = registries("a", false);
        var b = registries("b", true);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> RegistryContext.withRegistries(a, () -> SERIALIZER.fromJson(ID, json()).value.value()));
            var second = executor.submit(() -> RegistryContext.withRegistries(b, () -> SERIALIZER.fromJson(ID, json()).value.value()));
            assertEquals("a_value", first.get(5, TimeUnit.SECONDS));
            assertEquals("b_value", second.get(5, TimeUnit.SECONDS));
            assertNull(RegistryContext.scoped());
        } finally { executor.shutdownNow(); }
    }
}
