package com.leclowndu93150.thaumaturge.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CodecsTest {
    private final CodecReferenceProbe port = new CodecReferenceProbe(false);

    private void compare(String group, JsonObject actual) throws Exception {
        try (var stream = getClass().getResourceAsStream("dfu8-reference.json")) {
            assertNotNull(stream, "Generate the pinned DFU 8 reference with generateDfuReference");
            JsonObject expected = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject(group);
            assertEquals(expected.keySet(), actual.keySet(), group + " scenarios");
            for (String key : expected.keySet()) assertEquals(expected.get(key), actual.get(key), group + ": " + key);
        }
    }

    @Test void codecValidationMatchesTheActual1211Library() throws Exception { compare("validation", port.validations()); }
    @Test void mapValidationMatchesTheActual1211Library() throws Exception { compare("mapValidation", port.mapValidations()); }
    @Test void boundedListsMatchTheActual1211Library() throws Exception { compare("lists", port.lists()); }
    @Test void stringBoundsMatchTheActual1211Library() throws Exception { compare("strings", port.strings()); }
    @Test void alternativeSchemasMatchTheActual1211Library() throws Exception { compare("alternatives", port.alternatives()); }
    @Test void lazyConstructionMatchesTheActual1211Library() throws Exception { compare("lazy", port.lazyCodecs()); }
    @Test void recursiveSchemasMatchTheActual1211Library() throws Exception { compare("recursive", port.recursiveCodecs()); }

    @Test
    void concurrentFirstUseBuildsALazyCodecOnce() throws Exception {
        AtomicInteger builds = new AtomicInteger();
        Codec<Integer> codec = Codecs.lazyInitialized(() -> { builds.incrementAndGet(); return Codec.INT; });
        var executor = Executors.newFixedThreadPool(8);
        try {
            var calls = new ArrayList<Callable<Integer>>();
            for (int i = 0; i < 64; i++) calls.add(() -> codec.parse(JsonOps.INSTANCE,
                    JsonParser.parseString("7")).result().orElseThrow());
            for (var value : executor.invokeAll(calls)) assertEquals(7, value.get());
            assertEquals(1, builds.get());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
