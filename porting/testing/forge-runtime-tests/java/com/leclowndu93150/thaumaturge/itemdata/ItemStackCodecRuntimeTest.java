package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.recipe.AbstractRecipe;
import com.leclowndu93150.thaumaturge.recipe.SingleRecipeInput;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ItemStackCodecRuntimeTest {
    private static final ItemDataRegistry CATALOG = new ItemDataRegistry("port_stack_codec");
    private static final ItemDataKey<Integer> COUNT = CATALOG.<Integer>registerComponentType("count", b -> b.persistent(Codec.INT)).get();
    private static final ResourceKey<Registry<String>> VALUES = ResourceKey.createRegistryKey(new ResourceLocation("port_stack_codec", "values"));
    private static final ResourceKey<String> VALUE = ResourceKey.create(VALUES, new ResourceLocation("port_stack_codec", "value"));
    private static final ItemDataKey<Holder<String>> HOLDER = CATALOG.<Holder<String>>registerComponentType("holder", b -> b.persistent(RegistryFixedCodec.create(VALUES))).get();
    private static final Codec<ItemStack> CODEC = ItemStackCodecs.codec(id -> id.getNamespace().equals("minecraft") ? VanillaItemData.key(id) : CATALOG.get(id));
    private static final Capability<AtomicInteger> COUNTER = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation ITEM_ID = new ResourceLocation("port_stack_codec", "defaulted");
    private static final ResourceLocation RECIPE_TYPE_ID = new ResourceLocation("port_stack_codec", "result");
    private static final RecipeType<ResultRecipe> RECIPE_TYPE = new RecipeType<>() {};
    private static final SimpleRecipeSerializer<ResultRecipe> SERIALIZER = new SimpleRecipeSerializer<>(
            CODEC.fieldOf("result").xmap(ResultRecipe::new, recipe -> recipe.result),
            VanillaStreamCodecs.ITEM_STACK.map(ResultRecipe::new, recipe -> recipe.result));
    private static Item item;

    @BeforeAll
    static void registerNativeItemAndRecipeWithRealCapabilityAndConstructorDefaults() {
        var registry = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var view = (MappedRegistry<?>) net.minecraft.core.registries.BuiltInRegistries.ITEM;
        view.unfreeze(); registry.unfreeze();
        try {
            item = new Item(ItemDataDefaults.with(new Item.Properties().durability(100), COUNT, 7)) {
                @Override public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
                    return new ICapabilitySerializable<CompoundTag>() {
                        final AtomicInteger counter = new AtomicInteger(11);
                        final LazyOptional<AtomicInteger> value = LazyOptional.of(() -> counter);
                        @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                            return cap == COUNTER ? value.cast() : LazyOptional.empty();
                        }
                        @Override public CompoundTag serializeNBT() {
                            var tag = new CompoundTag(); tag.putInt("counter", counter.get()); return tag;
                        }
                        @Override public void deserializeNBT(CompoundTag tag) { counter.set(tag.getInt("counter")); }
                    };
                }
            };
            ForgeRegistries.ITEMS.register(ITEM_ID, item);
        } finally { registry.freeze(); view.freeze(); }
        ForgeRegistries.RECIPE_TYPES.register(RECIPE_TYPE_ID, RECIPE_TYPE);
        ForgeRegistries.RECIPE_SERIALIZERS.register(RECIPE_TYPE_ID, SERIALIZER);
    }

    private static JsonElement json(String text) { return JsonParser.parseString(text); }
    private static ItemStack parse(Codec<ItemStack> codec, String text) {
        return codec.parse(JsonOps.INSTANCE, json(text)).getOrThrow(false, message -> { throw new AssertionError(message); });
    }
    private static void fails(Codec<ItemStack> codec, String text) {
        var result = codec.parse(JsonOps.INSTANCE, json(text));
        assertTrue(result.error().isPresent(), text);
        assertTrue(result.result().isEmpty(), text);
    }

    @Test
    void sourceRecipeResultsUseLowercaseCountAndNamedComponents() {
        var stack = parse(CODEC, """
                {"id":"minecraft:enchanted_book","count":4,"components":{
                  "minecraft:stored_enchantments":{"levels":{"minecraft:fortune":1},"show_in_tooltip":false},
                  "minecraft:custom_name":"Ported"}}
                """);
        assertEquals(Items.ENCHANTED_BOOK, stack.getItem());
        assertEquals(4, stack.getCount());
        assertEquals("Ported", stack.getHoverName().getString());
        assertEquals(new EnchantmentData(Map.of(new ResourceLocation("minecraft", "fortune"), 1), false), ItemData.get(stack, VanillaItemData.STORED_ENCHANTMENTS));
        JsonObject encoded = CODEC.encodeStart(JsonOps.INSTANCE, stack).result().orElseThrow().getAsJsonObject();
        assertEquals(4, encoded.get("count").getAsInt());
        assertTrue(encoded.getAsJsonObject("components").has("minecraft:stored_enchantments"));
        assertFalse(encoded.has("Count")); assertFalse(encoded.has("tag"));
        assertEquals(ItemData.comparisonFields(stack), ItemData.comparisonFields(CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow()));
    }

    @Test
    void sourceCountFallbackIsRetainedAndEncodingRejectsOutOfRangeCounts() {
        assertEquals(1, parse(CODEC, "{\"id\":\"minecraft:paper\"}").getCount());
        for (String count : List.of("0", "-1", "100", "\"bad\"", "null", "false")) {
            assertEquals(1, parse(CODEC, "{\"id\":\"minecraft:paper\",\"count\":" + count + "}").getCount(), count);
        }
        assertEquals(99, parse(CODEC, "{\"id\":\"minecraft:paper\",\"count\":99}").getCount());
        assertTrue(CODEC.encodeStart(JsonOps.INSTANCE, new ItemStack(Items.PAPER, 100)).error().isPresent());
        assertTrue(CODEC.encodeStart(JsonOps.INSTANCE, ItemStack.EMPTY).error().isPresent());
    }

    @Test
    void invalidItemsAndPresentMalformedComponentsHaveNoPartialStack() {
        for (String text : List.of("{}", "{\"id\":\"minecraft:air\"}", "{\"id\":\"missing:not_an_item\"}",
                "{\"id\":\"minecraft:paper\",\"components\":false}",
                "{\"id\":\"minecraft:paper\",\"components\":{\"missing:field\":1}}",
                "{\"id\":\"minecraft:paper\",\"components\":{\"minecraft:damage\":-1}}",
                "{\"id\":\"minecraft:paper\",\"components\":{\"minecraft:damage\":2,\"!minecraft:damage\":{}}}")) fails(CODEC, text);
    }

    @Test
    void optionalCodecUsesOnlyAnEmptyObjectForAnEmptyStack() {
        var optional = ItemStackCodecs.optional(CODEC);
        assertSame(ItemStack.EMPTY, parse(optional, "{}"));
        assertEquals(json("{}"), optional.encodeStart(JsonOps.INSTANCE, ItemStack.EMPTY).result().orElseThrow());
        for (String text : List.of("null", "[]", "\"minecraft:paper\"", "{\"count\":1}", "{\"id\":\"minecraft:air\"}")) fails(optional, text);
        var paper = parse(ItemStackCodecs.OPTIONAL_CODEC, "{\"id\":\"minecraft:paper\",\"count\":3}");
        assertEquals(3, paper.getCount());
        assertEquals(3, ItemStackCodecs.OPTIONAL_CODEC.parse(NbtOps.INSTANCE,
                ItemStackCodecs.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, paper).result().orElseThrow()).result().orElseThrow().getCount());
    }

    @Test
    void removedNativeAndModDefaultsRoundTripAsComponents() {
        var stack = new ItemStack(item);
        ItemData.remove(stack, COUNT); ItemData.remove(stack, VanillaItemData.DAMAGE); ItemData.remove(stack, VanillaItemData.ENCHANTMENTS);
        var encoded = CODEC.encodeStart(JsonOps.INSTANCE, stack).result().orElseThrow().getAsJsonObject();
        var fields = encoded.getAsJsonObject("components");
        assertTrue(fields.has("!port_stack_codec:count")); assertTrue(fields.has("!minecraft:damage")); assertTrue(fields.has("!minecraft:enchantments"));
        var loaded = CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        assertNull(ItemData.get(loaded, COUNT)); assertFalse(loaded.isDamageableItem());
        assertNull(ItemData.get(loaded, VanillaItemData.ENCHANTMENTS));
        assertEquals(ItemData.comparisonFields(stack), ItemData.comparisonFields(loaded));
    }

    @Test
    void residualTypedNbtAndRealForgeCapabilitiesSurviveJsonAndNbtWithoutAliasing() throws Exception {
        var stack = new ItemStack(item, 3);
        stack.getCapability(COUNTER).orElseThrow(AssertionError::new).set(42);
        var tag = TagParser.parseTag("{Damage:17,HideFlags:3,Unbreakable:1b,display:{Name:'\"Saved\"',Lore:['\"Line\"'],color:12},other:{b:1b,s:2s,i:3,l:4L,f:0.5f,d:0.25d,ba:[B;1b,2b],ia:[I;3,4],la:[L;5L,6L]}}");
        stack.setTag(tag); ItemData.set(stack, COUNT, 9);
        for (var ops : List.of(JsonOps.INSTANCE, NbtOps.INSTANCE)) roundTripNativeState(stack, ops);
        var encoded = CODEC.encodeStart(NbtOps.INSTANCE, stack).result().orElseThrow();
        var loaded = CODEC.parse(NbtOps.INSTANCE, encoded).result().orElseThrow();
        ((CompoundTag) encoded).putString("forge_data", "{}");
        stack.getOrCreateTag().getCompound("other").putInt("i", 99);
        stack.getCapability(COUNTER).orElseThrow(AssertionError::new).set(99);
        assertEquals(3, loaded.getTag().getCompound("other").getInt("i"));
        assertEquals(42, loaded.getCapability(COUNTER).orElseThrow(AssertionError::new).get());
    }

    private <T> void roundTripNativeState(ItemStack stack, com.mojang.serialization.DynamicOps<T> ops) {
        T encoded = CODEC.encodeStart(ops, stack).result().orElseThrow();
        var loaded = CODEC.parse(ops, encoded).result().orElseThrow();
        assertEquals(stack.getCount(), loaded.getCount());
        var expected = stack.save(new CompoundTag());
        var actual = loaded.save(new CompoundTag());
        // Managed components may canonicalize JSON text; residual native data keeps its exact NBT types.
        VanillaItemData.stripNativeStorage(expected.getCompound("tag"));
        VanillaItemData.stripNativeStorage(actual.getCompound("tag"));
        assertEquals(expected, actual);
        assertEquals(42, loaded.getCapability(COUNTER).orElseThrow(AssertionError::new).get());
        assertEquals(9, ItemData.get(loaded, COUNT));
        assertEquals(ItemData.comparisonFields(stack), ItemData.comparisonFields(loaded));
    }

    @Test
    void malformedResidualDataAndComponentStorageConflictsFailExplicitly() {
        for (String carrier : List.of("false", "\"not snbt\"", "\"{id:foo}\"", "\"{tag:1}\"", "\"{tag:{Damage:4}}\"", "\"{tag:{\\\"thaumaturge:components\\\":{}}}\"")) {
            fails(CODEC, "{\"id\":\"minecraft:paper\",\"forge_data\":" + carrier + "}");
        }
        var stack = new ItemStack(item);
        var unknown = new CompoundTag(); unknown.putInt("port_stack_codec:missing", 3);
        stack.getOrCreateTag().put(ItemData.TAG, unknown);
        assertTrue(CODEC.encodeStart(JsonOps.INSTANCE, stack).error().isPresent());
    }

    private RegistryAccess.Frozen provider(String value) {
        var registry = new MappedRegistry<String>(VALUES, Lifecycle.stable());
        registry.register(VALUE, value, Lifecycle.stable());
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
    }

    @Test
    void registryOpsOwnDecodeApplyAndEncodeWithoutRetainingThePreviousWorld() {
        var first = provider("first"); var second = provider("second");
        var firstOps = RegistryOps.create(JsonOps.INSTANCE, first); var secondOps = RegistryOps.create(JsonOps.INSTANCE, second);
        var text = json("{\"id\":\"" + ITEM_ID + "\",\"components\":{\"port_stack_codec:holder\":\"port_stack_codec:value\"}}");
        var a = CODEC.parse(firstOps, text).result().orElseThrow();
        var b = CODEC.parse(secondOps, CODEC.encodeStart(secondOps, a).result().orElseThrow()).result().orElseThrow();
        assertEquals("first", ItemDataContext.withRegistries(first, () -> ItemData.get(a, HOLDER).value()));
        assertSame(second.registryOrThrow(VALUES).getHolderOrThrow(VALUE), ItemDataContext.withRegistries(second, () -> ItemData.get(b, HOLDER)));
        assertTrue(CODEC.parse(JsonOps.INSTANCE, text).error().isPresent());
        assertTrue(CODEC.encodeStart(JsonOps.INSTANCE, a).error().isPresent());
    }

    @Test
    void nestedInventoryUsesSourceItemSchemaAndRetainsSlotsAndOwnership() {
        var tool = new ItemStack(Items.IRON_PICKAXE, 2); tool.setDamageValue(17);
        ItemData.remove(tool, VanillaItemData.ENCHANTMENTS);
        ItemData.set(tool, VanillaItemData.CUSTOM_NAME, Component.literal("Nested"));
        var contents = ItemContents.fromItems(List.of(ItemStack.EMPTY, tool, new ItemStack(Items.PAPER, 4)));
        var encoded = ItemContents.CODEC.encodeStart(JsonOps.INSTANCE, contents).result().orElseThrow();
        assertEquals(2, encoded.getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject("item").get("count").getAsInt());
        var loaded = ItemContents.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        var target = new java.util.ArrayList<>(java.util.Collections.nCopies(4, ItemStack.EMPTY)); loaded.copyInto(target);
        assertTrue(target.get(0).isEmpty()); assertTrue(target.get(3).isEmpty());
        assertEquals(17, target.get(1).getDamageValue()); assertEquals("Nested", target.get(1).getHoverName().getString());
        assertNull(ItemData.get(target.get(1), VanillaItemData.ENCHANTMENTS)); assertEquals(4, target.get(2).getCount());
        tool.setDamageValue(22); target.get(1).setDamageValue(55); loaded.copyInto(target);
        assertEquals(17, target.get(1).getDamageValue());
    }

    public static final class ResultRecipe extends AbstractRecipe<SingleRecipeInput> {
        final ItemStack result;
        ResultRecipe(ItemStack result) { this.result = result; }
        @Override public boolean matches(SingleRecipeInput input, Level level) { return true; }
        @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return result.copy(); }
        @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result; }
        @Override public boolean canCraftInDimensions(int width, int height) { return true; }
        @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
        @Override public RecipeType<?> getType() { return RECIPE_TYPE; }
    }

    @Test
    void actualForgeRecipeSerializerLoadsResultComponentsAndNativeNetworkRetainsThem() {
        var id = new ResourceLocation("port_stack_codec", "recipe");
        var recipe = SERIALIZER.fromJson(id, json("{\"result\":{\"id\":\"minecraft:iron_pickaxe\",\"count\":2,\"components\":{\"minecraft:damage\":17}}}").getAsJsonObject());
        assertEquals(id, recipe.getId()); assertEquals(2, recipe.result.getCount()); assertEquals(17, recipe.result.getDamageValue());
        var wire = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            SERIALIZER.toNetwork(wire, recipe);
            var decoded = SERIALIZER.fromNetwork(id, wire);
            assertEquals(id, decoded.getId()); assertEquals(recipe.result.save(new CompoundTag()), decoded.result.save(new CompoundTag()));
            assertEquals(0, wire.readableBytes()); assertEquals(1, wire.refCnt());
        } finally { wire.release(); }
    }
}
