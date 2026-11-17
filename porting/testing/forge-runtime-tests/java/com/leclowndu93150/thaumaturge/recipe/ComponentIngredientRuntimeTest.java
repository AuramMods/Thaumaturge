package com.leclowndu93150.thaumaturge.recipe;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.itemdata.EnchantmentData;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataContext;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataKey;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataRegistry;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import com.leclowndu93150.thaumaturge.recipe.ingredient.CodecIngredientSerializer;
import com.leclowndu93150.thaumaturge.recipe.ingredient.ComponentIngredient;
import com.leclowndu93150.thaumaturge.recipe.ingredient.IngredientRegistration;
import com.leclowndu93150.thaumaturge.serialization.IngredientCodecs;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ComponentIngredientRuntimeTest {
    private static final ResourceLocation COMPONENTS = new ResourceLocation("neoforge", "components");
    private static final ResourceKey<Registry<String>> VALUES = ResourceKey.createRegistryKey(new ResourceLocation("port_fixture", "values"));
    private static final ResourceKey<String> VALUE = ResourceKey.create(VALUES, new ResourceLocation("port_fixture", "value"));
    private static ItemDataKey<Holder<String>> holderKey;
    private final RegistryAccess builtins = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    private static RegisterEvent event(ResourceKey<? extends Registry<?>> key, ForgeRegistry<?> forge, Registry<?> vanilla) throws Exception {
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        return ctor.newInstance(key, forge, vanilla);
    }

    @BeforeAll
    static void registerTheRealSchemasAndNativeSerializerEvent() throws Exception {
        ItemDataRegistry catalog = new ItemDataRegistry("port_fixture");
        holderKey = catalog.<Holder<String>>registerComponentType("holder", b -> b.persistent(RegistryFixedCodec.create(VALUES))).get();
        ItemData.registerCatalog(catalog);
        IngredientRegistration registration = new IngredientRegistration("thaumaturge");
        registration.register(COMPONENTS, () -> ComponentIngredient.SERIALIZER);
        var bus = BusBuilder.builder().build();
        registration.register(bus);
        bus.post(event(ForgeRegistries.Keys.RECIPE_SERIALIZERS, (ForgeRegistry<?>) ForgeRegistries.RECIPE_SERIALIZERS,
                BuiltInRegistries.RECIPE_SERIALIZER));
    }

    private EnchantmentData enchantments() {
        return new EnchantmentData(Map.of(new ResourceLocation("minecraft", "fortune"), 2), false);
    }

    private ItemStack book() {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        ItemData.set(stack, VanillaItemData.STORED_ENCHANTMENTS, enchantments());
        return stack;
    }

    private ComponentIngredient ingredient(boolean strict) {
        return ComponentIngredient.of(strict, VanillaItemData.STORED_ENCHANTMENTS, enchantments(), Items.ENCHANTED_BOOK);
    }

    private RegistryAccess.Frozen registries(String value) {
        MappedRegistry<String> registry = new MappedRegistry<>(VALUES, Lifecycle.stable());
        registry.register(VALUE, value, Lifecycle.stable());
        List<Registry<?>> all = new ArrayList<>();
        builtins.registries().forEach(entry -> all.add(entry.value()));
        all.add(registry);
        return new RegistryAccess.ImmutableRegistryAccess(all).freeze();
    }

    @Test
    void componentSerializerUsesTheBaselineIdInTheActualForgeRegistry() {
        assertSame(ComponentIngredient.SERIALIZER, CodecIngredientSerializer.registered(COMPONENTS));
        assertEquals(COMPONENTS, CraftingHelper.getID(ComponentIngredient.SERIALIZER));
        assertFalse(ingredient(false).isSimple());
        assertFalse(ingredient(false).isEmpty());
    }

    @Test
    void subsetPredicatesAllowOtherDataWithoutChangingTheCandidate() {
        var ingredient = ingredient(false);
        ItemStack matching = book();
        matching.getOrCreateTag().putString("addon", "extra");
        var before = matching.getTag().copy();
        assertTrue(ingredient.test(matching));
        assertEquals(before, matching.getTag());
        assertFalse(ingredient.test(new ItemStack(Items.ENCHANTED_BOOK)));
        assertFalse(ingredient.test(new ItemStack(Items.BOOK)));
        ItemData.set(matching, VanillaItemData.STORED_ENCHANTMENTS, new EnchantmentData(enchantments().levels(), true));
        assertFalse(ingredient.test(matching));
    }

    @Test
    void strictPredicatesCompareEffectiveDataAndIgnoreStackCounts() {
        var ingredient = ingredient(true);
        ItemStack matching = book();
        matching.setCount(12);
        assertTrue(ingredient.test(matching));
        matching.getOrCreateTag().putString("addon", "extra");
        assertFalse(ingredient.test(matching));
        var zeroDamage = ComponentIngredient.of(true, VanillaItemData.DAMAGE, 0, Items.IRON_PICKAXE);
        assertTrue(zeroDamage.test(new ItemStack(Items.IRON_PICKAXE)));
        ItemStack damaged = new ItemStack(Items.IRON_PICKAXE);
        damaged.setDamageValue(1);
        assertFalse(zeroDamage.test(damaged));
    }

    @Test
    void baselineJsonFieldsAndDefaultOmissionRoundTripThroughTheOwnedEntryPoint() {
        RegistryContext.withRegistries(builtins, () -> {
            var encoded = IngredientCodecs.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, builtins), ingredient(true)).result().orElseThrow();
            assertEquals("neoforge:components", encoded.getAsJsonObject().get("type").getAsString());
            assertTrue(encoded.getAsJsonObject().get("strict").getAsBoolean());
            var decoded = (ComponentIngredient) IngredientCodecs.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, builtins), encoded).result().orElseThrow();
            assertTrue(decoded.isStrict());
            assertTrue(decoded.test(book()));
            var defaults = IngredientCodecs.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, builtins), ingredient(false)).result().orElseThrow();
            assertFalse(defaults.getAsJsonObject().has("strict"));
            return null;
        });
    }

    @Test
    void genericNativeNbtOpsKeepStrictAndNestedBooleanValues() {
        var ops = RegistryOps.create(NbtOps.INSTANCE, builtins);
        var encoded = IngredientCodecs.CODEC.encodeStart(ops, ingredient(true)).result().orElseThrow();
        var decoded = (ComponentIngredient) IngredientCodecs.CODEC.parse(ops, encoded).result().orElseThrow();
        assertTrue(decoded.isStrict());
        assertFalse(decoded.components().expected().get(VanillaItemData.STORED_ENCHANTMENTS).showInTooltip());
        assertTrue(decoded.test(book()));
        ItemStack extra = book();
        extra.getOrCreateTag().putInt("extra", 1);
        assertFalse(decoded.test(extra));
    }

    @Test
    void compoundNativeNbtKeepsOwnedChildrenTypedAndPredicateSensitive() {
        Ingredient union = CompoundIngredient.of(ingredient(true), Ingredient.of(Items.COAL));
        var ops = RegistryOps.create(NbtOps.INSTANCE, builtins);
        var encoded = IngredientCodecs.CODEC.encodeStart(ops, union).result().orElseThrow();
        Ingredient decoded = IngredientCodecs.CODEC.parse(ops, encoded).result().orElseThrow();
        assertTrue(decoded.test(book()));
        assertTrue(decoded.test(new ItemStack(Items.COAL)));
        assertFalse(decoded.test(new ItemStack(Items.ENCHANTED_BOOK)));
    }

    @Test
    void explicitRegistryOpsOwnConstructionEvenInsideAnUnrelatedOuterScope() {
        var a = registries("outer");
        var b = registries("actual");
        RegistryContext.withRegistries(a, () -> {
            var input = JsonParser.parseString("{\"type\":\"neoforge:components\",\"items\":[\"minecraft:coal\"],"
                    + "\"components\":{\"port_fixture:holder\":\"port_fixture:value\"}}");
            var decoded = IngredientCodecs.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, b), input);
            assertTrue(decoded.result().isPresent(), () -> decoded.error().orElseThrow().message());
            var ingredient = (ComponentIngredient) decoded.result().orElseThrow();
            assertSame(b.registryOrThrow(VALUES).getHolderOrThrow(VALUE), ingredient.components().expected().get(holderKey));
            ItemDataContext.withRegistries(b, () -> {
                assertEquals("actual", ItemData.get(ingredient.getItems()[0], holderKey).value());
                return null;
            });
            assertSame(a, RegistryContext.scoped());
            assertTrue(RegistryFixedCodec.create(VALUES).encodeStart(ItemDataContext.ops(), a.registryOrThrow(VALUES).getHolderOrThrow(VALUE))
                    .result().isPresent());
            return null;
        });
        assertNull(RegistryContext.scoped());
    }

    @Test
    void malformedStrictFieldsAndComponentRemovalPredicatesFailExplicitly() {
        var ops = RegistryOps.create(JsonOps.INSTANCE, builtins);
        for (String fields : List.of("\"strict\":\"bad\",\"components\":{}", "\"components\":{\"!minecraft:damage\":{}}",
                "\"components\":{\"minecraft:no_such_component\":3}")) {
            var decoded = IngredientCodecs.CODEC.parse(ops, JsonParser.parseString(
                    "{\"type\":\"neoforge:components\",\"items\":[\"minecraft:coal\"]," + fields + "}"));
            assertTrue(decoded.error().isPresent());
            assertTrue(decoded.result().isEmpty());
        }
    }

    @Test
    void registryOpsRetainTheNativeLookupAndRestoreItAfterAnException() {
        var outer = registries("outer");
        var actual = registries("actual");
        var source = RegistryOps.create(JsonOps.INSTANCE, actual);
        RegistryContext.withRegistries(outer, () -> {
            assertThrows(IllegalStateException.class, () -> ItemDataContext.withOps(source, () -> {
                var nbt = (RegistryOps<?>) ItemDataContext.ops();
                assertSame(source.owner(VALUES).orElseThrow(), nbt.owner(VALUES).orElseThrow());
                assertSame(source.getter(VALUES).orElseThrow(), nbt.getter(VALUES).orElseThrow());
                throw new IllegalStateException("fixture");
            }));
            assertTrue(RegistryFixedCodec.create(VALUES).encodeStart(ItemDataContext.ops(), outer.registryOrThrow(VALUES).getHolderOrThrow(VALUE))
                    .result().isPresent());
            assertSame(outer, RegistryContext.scoped());
            return null;
        });
        assertNull(RegistryContext.scoped());
    }

    @Test
    void nativeForgeJsonCallbackUsesTheRegisteredOwnedSerializer() {
        RegistryContext.withRegistries(builtins, () -> {
            var json = ingredient(true).toJson();
            Ingredient decoded = Ingredient.fromJson(json);
            assertInstanceOf(ComponentIngredient.class, decoded);
            assertTrue(decoded.test(book()));
            assertFalse(decoded.test(new ItemStack(Items.ENCHANTED_BOOK)));
            return null;
        });
    }

    @Test
    void nativeWireProtocolKeepsTypedHoldersWithAnExplicitBufferBeforeAWorldExists() {
        var lookup = registries("network");
        ComponentIngredient source = RegistryContext.withRegistries(lookup, () -> ComponentIngredient.of(false, holderKey,
                lookup.registryOrThrow(VALUES).getHolderOrThrow(VALUE), Items.COAL));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), lookup);
        try {
            VanillaStreamCodecs.INGREDIENT.encode(buffer, source);
            var received = (ComponentIngredient) VanillaStreamCodecs.INGREDIENT.decode(buffer);
            assertEquals("network", received.components().expected().get(holderKey).value());
            assertSame(lookup.registryOrThrow(VALUES).getHolderOrThrow(VALUE), received.components().expected().get(holderKey));
            assertEquals(0, buffer.readableBytes());
            assertEquals(1, buffer.refCnt());
        } finally { buffer.release(); }
    }

    @Test
    void itemTagSelectionsUseActualNativeBindingsAndKeepTheirSymbolicIdentity() {
        var registry = BuiltInRegistries.ITEM;
        Map<TagKey<Item>, List<Holder<Item>>> saved = new LinkedHashMap<>();
        registry.getTags().forEach(pair -> saved.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var tag = TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation("port_fixture", "selected"));
        var bound = new LinkedHashMap<>(saved);
        bound.put(tag, List.of(Items.COAL.builtInRegistryHolder(), Items.IRON_INGOT.builtInRegistryHolder()));
        registry.bindTags(bound);
        try {
            var ops = RegistryOps.create(JsonOps.INSTANCE, builtins);
            var input = JsonParser.parseString("{\"type\":\"neoforge:components\",\"items\":\"#port_fixture:selected\",\"components\":{}}");
            Ingredient decoded = IngredientCodecs.CODEC.parse(ops, input).result().orElseThrow();
            assertTrue(decoded.test(new ItemStack(Items.COAL)));
            assertTrue(decoded.test(new ItemStack(Items.IRON_INGOT)));
            assertFalse(decoded.test(new ItemStack(Items.GOLD_INGOT)));
            assertEquals("#port_fixture:selected", IngredientCodecs.CODEC.encodeStart(ops, decoded).result().orElseThrow()
                    .getAsJsonObject().get("items").getAsString());
        } finally { registry.bindTags(saved); }
    }

    @Test
    void registrationIsLazyAndRunsOnTheActualSerializerEvent() throws Exception {
        AtomicInteger builds = new AtomicInteger();
        var registration = new IngredientRegistration("port_fixture");
        var entry = registration.register("lifecycle", () -> { builds.incrementAndGet(); return new CodecIngredientSerializer<>(ComponentIngredient.CODEC); });
        var bus = BusBuilder.builder().build();
        registration.register(bus);
        bus.post(event(ForgeRegistries.Keys.ITEMS, (ForgeRegistry<?>) ForgeRegistries.ITEMS, BuiltInRegistries.ITEM));
        assertEquals(0, builds.get());
        bus.post(event(ForgeRegistries.Keys.RECIPE_SERIALIZERS, (ForgeRegistry<?>) ForgeRegistries.RECIPE_SERIALIZERS,
                BuiltInRegistries.RECIPE_SERIALIZER));
        assertEquals(1, builds.get());
        assertEquals(entry.getId(), CraftingHelper.getID(entry.get()));
        assertSame(entry.get(), CodecIngredientSerializer.registered(entry.getId()));
        assertEquals(1, builds.get());
    }

    @Test
    void duplicateNativeRegistrationFailsWithoutOverwritingTheOwnedBinding() {
        ResourceLocation id = new ResourceLocation("port_fixture", "duplicate");
        var original = new CodecIngredientSerializer<>(ComponentIngredient.CODEC);
        CodecIngredientSerializer.register(id, original);
        var replacement = new CodecIngredientSerializer<>(ComponentIngredient.CODEC);
        assertThrows(IllegalStateException.class, () -> CodecIngredientSerializer.register(id, replacement));
        assertSame(original, CodecIngredientSerializer.registered(id));
        assertEquals(id, CraftingHelper.getID(original));
        assertNull(CraftingHelper.getID(replacement));
    }

    @Test
    void actualNativeShapelessCraftingUsesTheRichPredicateInsteadOfDisplayItems() {
        var recipe = new ShapelessRecipe(new ResourceLocation("port_fixture", "rich"), "", CraftingBookCategory.MISC,
                new ItemStack(Items.STONE), NonNullList.of(Ingredient.EMPTY, ingredient(false), Ingredient.of(Items.COAL)));
        assertTrue(recipe.matches(CraftingInput.of(2, 1, List.of(book(), new ItemStack(Items.COAL))), null));
        assertFalse(recipe.matches(CraftingInput.of(2, 1, List.of(new ItemStack(Items.ENCHANTED_BOOK), new ItemStack(Items.COAL))), null));
    }
}
