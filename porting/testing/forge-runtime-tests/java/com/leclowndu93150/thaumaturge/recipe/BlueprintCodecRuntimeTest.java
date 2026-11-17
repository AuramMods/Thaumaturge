package com.leclowndu93150.thaumaturge.recipe;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintSource;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import org.junit.jupiter.api.Test;

public class BlueprintCodecRuntimeTest {
    private List<BlueprintSource> sources() {
        return List.of(new BlueprintSource.BlockSource(Blocks.STONE),
                new BlueprintSource.StateSource(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST)),
                new BlueprintSource.TagSource(BlockTags.BASE_STONE_OVERWORLD));
    }

    private List<BlueprintTarget> targets() {
        return List.of(BlueprintTarget.Keep.INSTANCE, BlueprintTarget.Air.INSTANCE,
                new BlueprintTarget.BlockTarget(Blocks.OAK_STAIRS, true, true),
                new BlueprintTarget.StateTarget(Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST)));
    }

    @Test
    void actualSourceVariantsKeepTheirFlatTypeTaggedJsonAndCompressedRoundTrips() {
        for (var source : sources()) {
            var json = BlueprintSource.CODEC.encodeStart(JsonOps.INSTANCE, source).result().orElseThrow();
            assertEquals(source.kind().getSerializedName(), json.getAsJsonObject().get("type").getAsString());
            assertFalse(json.getAsJsonObject().has("value"));
            assertEquals(source, BlueprintSource.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow());
            var compressed = BlueprintSource.CODEC.encodeStart(JsonOps.COMPRESSED, source).result().orElseThrow();
            assertEquals(source, BlueprintSource.CODEC.parse(JsonOps.COMPRESSED, compressed).result().orElseThrow());
        }
        assertEquals(new BlueprintSource.BlockSource(Blocks.STONE), BlueprintSource.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"type\":\"block\",\"block\":\"minecraft:stone\"}")).result().orElseThrow());
    }

    @Test
    void actualNonStackTargetsKeepTheirTypesFlagsDefaultsAndCompressedRoundTrips() {
        for (var target : targets()) {
            var json = BlueprintTarget.CODEC.encodeStart(JsonOps.INSTANCE, target).result().orElseThrow();
            assertEquals(target.kind().getSerializedName(), json.getAsJsonObject().get("type").getAsString());
            assertFalse(json.getAsJsonObject().has("value"));
            assertEquals(target, BlueprintTarget.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow());
            var compressed = BlueprintTarget.CODEC.encodeStart(JsonOps.COMPRESSED, target).result().orElseThrow();
            assertEquals(target, BlueprintTarget.CODEC.parse(JsonOps.COMPRESSED, compressed).result().orElseThrow());
        }
        assertEquals(new BlueprintTarget.BlockTarget(Blocks.STONE, false, false), BlueprintTarget.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"type\":\"block\",\"block\":\"minecraft:stone\"}")).result().orElseThrow());
    }

    @Test
    void allActualSourceAndTargetVariantsRoundTripTheOriginalWireCodecs() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            for (var source : sources()) {
                BlueprintSource.STREAM_CODEC.encode(buffer, source);
                assertEquals(source, BlueprintSource.STREAM_CODEC.decode(buffer));
                assertEquals(0, buffer.readableBytes());
                buffer.clear();
            }
            for (var target : targets()) {
                BlueprintTarget.STREAM_CODEC.encode(buffer, target);
                assertEquals(target, BlueprintTarget.STREAM_CODEC.decode(buffer));
                assertEquals(0, buffer.readableBytes());
                buffer.clear();
            }
            ItemStack stack = new ItemStack(Items.COAL, 7);
            stack.getOrCreateTag().putString("fixture", "preserved");
            BlueprintTarget.STREAM_CODEC.encode(buffer, new BlueprintTarget.StackTarget(stack));
            var decoded = (BlueprintTarget.StackTarget) BlueprintTarget.STREAM_CODEC.decode(buffer);
            assertTrue(ItemStack.isSameItemSameTags(stack, decoded.stack()));
            assertEquals(7, decoded.stack().getCount());
            assertEquals(0, buffer.readableBytes());
            assertEquals(1, buffer.refCnt());
        } finally { buffer.release(); }
    }

    @Test
    void actualBlueprintJsonRejectsUnknownTypesMissingFieldsAndMalformedFlags() {
        for (String json : List.of("{}", "{\"type\":\"unknown\"}", "{\"type\":\"block\"}",
                "{\"type\":\"block\",\"block\":\"minecraft:stone\",\"opposite\":\"bad\"}")) {
            assertTrue(BlueprintTarget.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).error().isPresent(), json);
        }
        assertTrue(BlueprintSource.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"type\":\"tag\"}")).error().isPresent());
    }
}
