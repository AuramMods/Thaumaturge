package com.leclowndu93150.thaumaturge.recipe;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.serialization.IngredientCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IngredientCodecRuntimeTest {
    @BeforeAll
    static void registerActualForgeSerializersNormallyRegisteredByForgeMod() {
        CraftingHelper.register(new ResourceLocation("forge", "nbt"), StrictNBTIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("forge", "partial_nbt"), PartialNBTIngredient.Serializer.INSTANCE);
        CraftingHelper.register(new ResourceLocation("forge", "compound"), CompoundIngredient.Serializer.INSTANCE);
    }

    private Ingredient parse(String json) {
        return IngredientCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow();
    }

    private JsonElement json(Ingredient ingredient) {
        return IngredientCodecs.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).result().orElseThrow();
    }

    private ItemStack taggedCoal(String value) {
        ItemStack stack = new ItemStack(Items.COAL, 4);
        stack.getOrCreateTag().putString("fixture", value);
        return stack;
    }

    @Test
    void actualNativeItemIngredientsKeepTheirSchemaAndCountIndependentMatching() {
        String input = "{\"item\":\"minecraft:coal\"}";
        Ingredient ingredient = parse(input);
        assertEquals(JsonParser.parseString(input), json(ingredient));
        assertTrue(ingredient.test(new ItemStack(Items.COAL, 32)));
        assertTrue(ingredient.test(taggedCoal("extra_data")));
        assertFalse(ingredient.test(new ItemStack(Items.IRON_INGOT)));
        assertTrue(ingredient.isSimple());
    }

    @Test
    void actualAlternativeIngredientsPreserveEveryAcceptedItem() {
        Ingredient ingredient = parse("[{\"item\":\"minecraft:coal\"},{\"item\":\"minecraft:iron_ingot\"}]");
        assertTrue(ingredient.test(new ItemStack(Items.COAL)));
        assertTrue(ingredient.test(new ItemStack(Items.IRON_INGOT)));
        assertFalse(ingredient.test(new ItemStack(Items.GOLD_INGOT)));
        assertEquals(2, json(ingredient).getAsJsonArray().size());
        Ingredient decoded = IngredientCodecs.CODEC.parse(JsonOps.INSTANCE, json(ingredient)).result().orElseThrow();
        assertTrue(decoded.test(new ItemStack(Items.COAL)));
        assertTrue(decoded.test(new ItemStack(Items.IRON_INGOT)));
    }

    @Test
    void tagIdentityAndTheExplicitEmptyIngredientArePreserved() {
        String tag = "{\"tag\":\"minecraft:planks\"}";
        assertEquals(JsonParser.parseString(tag), json(parse(tag)));
        Ingredient empty = parse("[]");
        assertSame(Ingredient.EMPTY, empty);
        assertEquals(JsonParser.parseString("[]"), json(empty));
        assertTrue(empty.test(ItemStack.EMPTY));
        assertFalse(empty.test(new ItemStack(Items.COAL)));
    }

    @Test
    void malformedAndUnknownEntriesRemainErrorsRatherThanSuccessfulDefaults() {
        for (String input : List.of("null", "7", "\"minecraft:coal\"", "{}", "[[{\"item\":\"minecraft:coal\"}]]",
                "{\"item\":\"thaumaturge:no_such_item\"}", "{\"item\":\"minecraft:air\"}",
                "{\"item\":\"minecraft:coal\",\"tag\":\"minecraft:planks\"}", "{\"type\":\"thaumaturge:no_such_ingredient\"}")) {
            var decoded = IngredientCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(input));
            assertTrue(decoded.error().isPresent(), input);
            assertTrue(decoded.result().isEmpty(), input);
        }
    }

    @Test
    void malformedAlternativesRetainTheirErrorValidPartialAndRejectedRemainder() {
        var decoded = IngredientCodecs.CODEC.decode(JsonOps.INSTANCE,
                JsonParser.parseString("[{\"item\":\"minecraft:coal\"},{\"item\":\"thaumaturge:no_such_item\"} ]"));
        List<String> errors = new ArrayList<>();
        var partial = decoded.resultOrPartial(errors::add).orElseThrow();
        assertTrue(decoded.result().isEmpty());
        assertFalse(errors.isEmpty());
        assertTrue(partial.getFirst().test(new ItemStack(Items.COAL)));
        assertFalse(partial.getFirst().test(new ItemStack(Items.IRON_INGOT)));
        assertEquals(JsonParser.parseString("[{\"item\":\"thaumaturge:no_such_item\"}]"), partial.getSecond());
    }

    @Test
    void nativeNbtOpsAndExistingEncoderPrefixesRetainTheirData() {
        Ingredient ingredient = parse("{\"item\":\"minecraft:coal\"}");
        var nbt = IngredientCodecs.CODEC.encodeStart(NbtOps.INSTANCE, ingredient).result().orElseThrow();
        assertTrue(IngredientCodecs.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow().test(new ItemStack(Items.COAL)));
        var prefix = JsonParser.parseString("{\"addon\":\"keep\"}");
        var encoded = IngredientCodecs.CODEC.encode(ingredient, JsonOps.INSTANCE, prefix).result().orElseThrow();
        assertEquals("keep", encoded.getAsJsonObject().get("addon").getAsString());
        assertEquals("minecraft:coal", encoded.getAsJsonObject().get("item").getAsString());
    }

    @Test
    void registeredForgeCustomPredicatesSurviveJsonAndNativeWireSerialization() {
        Ingredient expected = StrictNBTIngredient.of(taggedCoal("required"));
        Ingredient ingredient = IngredientCodecs.CODEC.parse(JsonOps.INSTANCE, json(expected)).result().orElseThrow();
        assertFalse(ingredient.isSimple());
        assertTrue(ingredient.test(taggedCoal("required")));
        assertFalse(ingredient.test(taggedCoal("wrong")));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        try {
            VanillaStreamCodecs.INGREDIENT.encode(buffer, ingredient);
            Ingredient received = VanillaStreamCodecs.INGREDIENT.decode(buffer);
            assertTrue(received.test(taggedCoal("required")));
            assertFalse(received.test(taggedCoal("wrong")));
            assertEquals(0, buffer.readableBytes());
            assertEquals(1, buffer.refCnt());
        } finally { buffer.release(); }
    }

    @Test
    void nestedNativeUnionsEncodeAsFlatAlternativesWithoutLosingCustomPredicates() {
        Ingredient ingredient = CompoundIngredient.of(Ingredient.of(Items.IRON_INGOT, Items.GOLD_INGOT),
                StrictNBTIngredient.of(taggedCoal("required")));
        JsonElement encoded = json(ingredient);
        assertTrue(encoded.isJsonArray());
        for (var entry : encoded.getAsJsonArray()) assertTrue(entry.isJsonObject());
        Ingredient decoded = IngredientCodecs.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        assertTrue(decoded.test(new ItemStack(Items.IRON_INGOT)));
        assertTrue(decoded.test(new ItemStack(Items.GOLD_INGOT)));
        assertTrue(decoded.test(taggedCoal("required")));
        assertFalse(decoded.test(taggedCoal("wrong")));
        var nbt = IngredientCodecs.CODEC.encodeStart(NbtOps.INSTANCE, ingredient).result().orElseThrow();
        assertFalse(IngredientCodecs.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow().test(taggedCoal("wrong")));
    }
}
