package com.leclowndu93150.thaumaturge.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.junit.jupiter.api.Test;

class BlockCodecsTest {
    @Test
    void simpleCodecKeepsTheRequiredPropertiesObject() {
        var codec = BlockCodecs.simpleCodec(p -> Blocks.STONE).codec();
        var encoded = codec.encodeStart(JsonOps.INSTANCE, Blocks.STONE).result().orElseThrow();
        assertEquals(JsonParser.parseString("{\"properties\":{}}"), encoded);
        assertSame(Blocks.STONE, codec.parse(JsonOps.INSTANCE, encoded).result().orElseThrow());
        assertTrue(codec.parse(JsonOps.INSTANCE, new JsonObject()).error().isPresent());
    }

    @Test
    void everyDecodeSuppliesFreshNativePropertiesToTheFactory() {
        List<BlockBehaviour.Properties> properties = new ArrayList<>();
        var codec = BlockCodecs.simpleCodec(p -> {
            properties.add(p);
            return Blocks.STONE;
        }).codec();
        var json = JsonParser.parseString("{\"properties\":{}}");
        codec.parse(JsonOps.INSTANCE, json).result().orElseThrow();
        codec.parse(JsonOps.INSTANCE, json).result().orElseThrow();
        assertEquals(2, properties.size());
        assertNotSame(properties.get(0), properties.get(1));
        assertNotNull(properties.get(0));
    }

    @Test
    void propertiesUseTheBaselinesUnitSchemaWithoutPersistingRuntimeFields() {
        var codec = BlockCodecs.simpleCodec(p -> Blocks.STONE).codec();
        var json = JsonParser.parseString("{\"properties\":{\"hardness\":999,\"unknown\":true}}");
        Block decoded = codec.parse(JsonOps.INSTANCE, json).result().orElseThrow();
        assertEquals(JsonParser.parseString("{\"properties\":{}}"),
                codec.encodeStart(JsonOps.INSTANCE, decoded).result().orElseThrow());
    }

    @Test
    void invalidPropertiesDoNotInvokeTheFactory() {
        var calls = new AtomicInteger();
        var codec = BlockCodecs.simpleCodec(p -> {
            calls.incrementAndGet();
            return Blocks.STONE;
        }).codec();
        for (String json : List.of("{}", "{\"properties\":42}", "{\"properties\":null}")) {
            assertTrue(codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).error().isPresent(), json);
        }
        assertEquals(0, calls.get());
    }

    @Test
    void customConstructorFieldsRemainRequiredAndRoundTripWithProperties() {
        MapCodec<Block> map = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.BOOL.fieldOf("input").forGetter(block -> block == Blocks.STONE),
                        BlockCodecs.<Block>propertiesCodec())
                .apply(instance, (input, properties) -> input ? Blocks.STONE : Blocks.AIR));
        var codec = map.codec();
        var input = JsonParser.parseString("{\"input\":true,\"properties\":{}}");
        var output = JsonParser.parseString("{\"input\":false,\"properties\":{}}");
        assertSame(Blocks.STONE, codec.parse(JsonOps.INSTANCE, input).result().orElseThrow());
        assertSame(Blocks.AIR, codec.parse(JsonOps.INSTANCE, output).result().orElseThrow());
        assertEquals(input, codec.encodeStart(JsonOps.INSTANCE, Blocks.STONE).result().orElseThrow());
        assertEquals(output, codec.encodeStart(JsonOps.INSTANCE, Blocks.AIR).result().orElseThrow());
        assertTrue(codec.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"properties\":{}}")).error().isPresent());
    }

    @Test
    void factoryFailurePropagates() {
        var codec = BlockCodecs.<Block>simpleCodec(p -> {
            throw new IllegalStateException("constructor failed");
        }).codec();
        var exception = assertThrows(IllegalStateException.class,
                () -> codec.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"properties\":{}}")));
        assertEquals("constructor failed", exception.getMessage());
    }
}
