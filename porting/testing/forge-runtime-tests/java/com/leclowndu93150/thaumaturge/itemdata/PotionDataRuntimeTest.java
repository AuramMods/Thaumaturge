package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.junit.jupiter.api.Test;

public class PotionDataRuntimeTest {
    private PotionData full() {
        return new PotionData(Optional.of(BuiltInRegistries.POTION.wrapAsHolder(Potions.SWIFTNESS)),
                Optional.of(0xff123456), List.of(EffectCodecs.instance(MobEffects.POISON, -1, 255, true, false, true)));
    }

    private PotionData parse(String input) {
        return PotionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(input)).result().orElseThrow();
    }

    @Test
    void codecRetainsTheSourceFullAndShorthandSchemasAndStrictFailures() {
        assertEquals(new PotionData(Potions.WATER), parse("\"minecraft:water\""));
        assertEquals(PotionData.EMPTY, parse("{}"));
        var value = full();
        var encoded = PotionData.CODEC.encodeStart(JsonOps.INSTANCE, value).result().orElseThrow();
        assertEquals("minecraft:swiftness", encoded.getAsJsonObject().get("potion").getAsString());
        assertEquals(-1, encoded.getAsJsonObject().getAsJsonArray("custom_effects").get(0).getAsJsonObject().get("amplifier").getAsInt());
        assertEquals(value, parse(encoded.toString()));
        var nbt = PotionData.CODEC.encodeStart(NbtOps.INSTANCE, value).result().orElseThrow();
        assertEquals(value, PotionData.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow());
        for (String json : List.of("\"thaumaturge:missing\"", "{\"potion\":\"thaumaturge:missing\"}",
                "{\"custom_color\":\"bad\"}", "{\"custom_effects\":[{\"id\":\"minecraft:speed\"},{\"id\":\"thaumaturge:missing\"}]}")) {
            var result = PotionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
            assertTrue(result.error().isPresent(), json);
            assertTrue(result.result().isEmpty(), json);
        }
    }

    @Test
    void nativePotionAndArrowDefaultsHavePresenceWithoutMutatingTheStack() {
        for (var item : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
            var stack = new ItemStack(item);
            assertEquals(PotionData.EMPTY, ItemData.get(stack, VanillaItemData.POTION_CONTENTS));
            assertTrue(ItemData.has(stack, VanillaItemData.POTION_CONTENTS));
            assertNull(stack.getTag());
        }
        assertNull(ItemData.get(new ItemStack(Items.COAL), VanillaItemData.POTION_CONTENTS));
        assertNull(ItemData.get(ItemStack.EMPTY, VanillaItemData.POTION_CONTENTS));
        assertEquals(new PotionData(Potions.WATER), ItemData.get(Items.POTION.getDefaultInstance(), VanillaItemData.POTION_CONTENTS));
    }

    @Test
    void nativePotionConsumersSeeBaseCustomEffectsAndColorAndReplacementDoesNotAppend() {
        var stack = new ItemStack(Items.SPLASH_POTION);
        stack.getOrCreateTag().putString("fixture", "preserved");
        stack.setHoverName(net.minecraft.network.chat.Component.literal("fixture"));
        ItemData.set(stack, VanillaItemData.POTION_CONTENTS, full());
        assertSame(Potions.SWIFTNESS, PotionUtils.getPotion(stack));
        assertEquals(2, PotionUtils.getMobEffects(stack).size());
        assertEquals(255, PotionUtils.getCustomEffects(stack).get(0).getAmplifier());
        assertEquals(0xff123456, PotionUtils.getColor(stack));
        assertEquals(full(), ItemData.get(stack, VanillaItemData.POTION_CONTENTS));
        ItemData.set(stack, VanillaItemData.POTION_CONTENTS, new PotionData(Potions.WATER));
        assertSame(Potions.WATER, PotionUtils.getPotion(stack));
        assertTrue(PotionUtils.getCustomEffects(stack).isEmpty());
        assertFalse(stack.getTag().contains("CustomPotionColor"));
        assertEquals("preserved", stack.getTag().getString("fixture"));
        assertEquals("fixture", stack.getHoverName().getString());
    }

    @Test
    void vanillaNbtWrittenOutsideTheBackendIsReadThroughTheRealNativeHelpers() {
        var stack = new ItemStack(Items.TIPPED_ARROW);
        PotionUtils.setPotion(stack, Potions.POISON);
        PotionUtils.setCustomEffects(stack, List.of(EffectCodecs.instance(MobEffects.MOVEMENT_SPEED, 40, 128)));
        stack.getOrCreateTag().putInt("CustomPotionColor", 0x112233);
        var value = ItemData.get(stack, VanillaItemData.POTION_CONTENTS);
        assertEquals(Potions.POISON, value.potion().orElseThrow().value());
        assertEquals(128, value.customEffects().get(0).getAmplifier());
        assertEquals(0x112233, value.getColor());
    }

    @Test
    void nativeStackSaveLoadAndCopyRetainValuesAndCopiesOwnTheirNbt() {
        var source = new ItemStack(Items.LINGERING_POTION, 3);
        ItemData.set(source, VanillaItemData.POTION_CONTENTS, full());
        var saved = source.save(new CompoundTag());
        var loaded = ItemStack.of(saved);
        assertEquals(3, loaded.getCount());
        assertEquals(full(), ItemData.get(loaded, VanillaItemData.POTION_CONTENTS));
        var copy = source.copy();
        ItemData.set(copy, VanillaItemData.POTION_CONTENTS, new PotionData(Potions.HEALING));
        assertEquals(full(), ItemData.get(source, VanillaItemData.POTION_CONTENTS));
        assertSame(Potions.HEALING, PotionUtils.getPotion(copy));
    }

    @Test
    void removingAndRestoringDefaultComponentsSurvivesNativeCopyAndSaveLoad() {
        for (var item : List.of(Items.POTION, Items.TIPPED_ARROW)) {
            var stack = new ItemStack(item);
            ItemData.set(stack, VanillaItemData.POTION_CONTENTS, full());
            assertEquals(full(), ItemData.remove(stack, VanillaItemData.POTION_CONTENTS));
            assertFalse(ItemData.has(stack, VanillaItemData.POTION_CONTENTS));
            assertTrue(PotionUtils.getMobEffects(stack).isEmpty());
            assertNull(ItemData.get(stack.copy(), VanillaItemData.POTION_CONTENTS));
            assertNull(ItemData.get(ItemStack.of(stack.save(new CompoundTag())), VanillaItemData.POTION_CONTENTS));
            ItemData.set(stack, VanillaItemData.POTION_CONTENTS, PotionData.EMPTY);
            assertEquals(PotionData.EMPTY, ItemData.get(stack, VanillaItemData.POTION_CONTENTS));
            assertNull(stack.getTag());
        }
    }

    @Test
    void explicitEmptyAndRemovalOnOtherItemsHaveDistinctPersistentPresence() {
        var stack = new ItemStack(Items.COAL);
        ItemData.set(stack, VanillaItemData.POTION_CONTENTS, PotionData.EMPTY);
        assertTrue(ItemData.has(stack, VanillaItemData.POTION_CONTENTS));
        assertEquals(PotionData.EMPTY, ItemData.get(ItemStack.of(stack.save(new CompoundTag())), VanillaItemData.POTION_CONTENTS));
        ItemData.remove(stack, VanillaItemData.POTION_CONTENTS);
        assertFalse(ItemData.has(stack, VanillaItemData.POTION_CONTENTS));
        assertNull(stack.getTag());
    }

    @Test
    void subsequentVanillaWritesOverrideATombstoneWithoutAStaleRead() {
        var stack = new ItemStack(Items.POTION);
        ItemData.remove(stack, VanillaItemData.POTION_CONTENTS);
        PotionUtils.setPotion(stack, Potions.WATER);
        assertEquals(new PotionData(Potions.WATER), ItemData.get(stack, VanillaItemData.POTION_CONTENTS));
        var clean = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        assertEquals(ItemData.comparisonFields(clean), ItemData.comparisonFields(stack));
    }

    @Test
    void explicitDataCopyReplacesTheNativeComponentAsAGroupIncludingRemoval() {
        var source = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        var destination = new ItemStack(Items.POTION);
        destination.getOrCreateTag().putString("other", "retained");
        ItemData.set(destination, VanillaItemData.POTION_CONTENTS, full());
        ItemData.copyExplicitData(source, destination);
        assertEquals(new PotionData(Potions.WATER), ItemData.get(destination, VanillaItemData.POTION_CONTENTS));
        assertEquals("retained", destination.getTag().getString("other"));
        ItemData.remove(source, VanillaItemData.POTION_CONTENTS);
        ItemData.copyExplicitData(source, destination);
        assertNull(ItemData.get(destination, VanillaItemData.POTION_CONTENTS));
        assertTrue(PotionUtils.getMobEffects(destination).isEmpty());
    }

    @Test
    void componentPatchJsonAndOriginalWireBridgeApplyPotionValuesAndRemovals() {
        var patch = ItemDataPatch.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
                "{\"minecraft:potion_contents\":{\"potion\":\"minecraft:water\"}}")).result().orElseThrow();
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        try {
            ItemDataPatch.STREAM_CODEC.encode(buffer, patch);
            var decoded = ItemDataPatch.STREAM_CODEC.decode(buffer);
            assertEquals(patch, decoded);
            var stack = ItemData.create(Items.POTION, 1, decoded);
            assertTrue(decoded.test(stack));
            assertSame(Potions.WATER, PotionUtils.getPotion(stack));
            var remove = ItemDataPatch.builder().remove(VanillaItemData.POTION_CONTENTS).build();
            remove.apply(stack);
            assertTrue(remove.test(stack));
            assertFalse(decoded.test(stack));
            assertEquals(0, buffer.readableBytes());
            assertEquals(1, buffer.refCnt());
        } finally { buffer.release(); }
    }

    @Test
    void effectAggregationAndCallbacksPreserveSourceOrderingAndCopyBehavior() {
        var value = full();
        List<MobEffectInstance> all = new ArrayList<>();
        value.getAllEffects().forEach(all::add);
        assertSame(MobEffects.MOVEMENT_SPEED, all.get(0).getEffect());
        assertSame(MobEffects.POISON, all.get(1).getEffect());
        assertTrue(value.hasEffects());
        assertFalse(value.is(BuiltInRegistries.POTION.wrapAsHolder(Potions.SWIFTNESS)));
        assertTrue(new PotionData(Potions.WATER).is(BuiltInRegistries.POTION.wrapAsHolder(Potions.WATER)));
        var original = value.customEffects().get(0);
        var another = value.customEffects().get(0);
        assertNotSame(original, another);
        value.forEachEffect(effect -> { assertFalse(all.stream().anyMatch(candidate -> candidate == effect)); });
    }

    @Test
    void sourceColorMathUsesIntegerWeightsVisibilityAndAnOpaqueBaseColor() {
        var red = new MobEffect(MobEffectCategory.NEUTRAL, 0xff0000) {};
        var blue = new MobEffect(MobEffectCategory.NEUTRAL, 0x0000ff) {};
        net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.register(new net.minecraft.resources.ResourceLocation("port_fixture", "color_red"), red);
        net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.register(new net.minecraft.resources.ResourceLocation("port_fixture", "color_blue"), blue);
        var value = new PotionData(Optional.empty(), Optional.empty(), List.of(
                EffectCodecs.instance(red, 20, 0), EffectCodecs.instance(blue, 20, 2)));
        assertEquals(0xff3f00bf, value.getColor());
        assertEquals(0xff385dc6, PotionData.EMPTY.getColor());
        assertTrue(PotionData.getColorOptional(List.of(EffectCodecs.instance(red, 20, 0, false, false))).isEmpty());
        assertEquals(0xff123456, full().getColor());
    }
}
