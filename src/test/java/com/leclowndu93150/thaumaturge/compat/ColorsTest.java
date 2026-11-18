package com.leclowndu93150.thaumaturge.compat;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ColorsTest {
    private void compare(Set<String> methods) throws Exception {
        var probe = new ColorReferenceProbe(false);
        try (var stream = ColorsTest.class.getResourceAsStream("colors-1.21.1-reference.json")) {
            assertNotNull(stream);
            var observations = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
            int checked = 0;
            for (var entry : observations) {
                var observation = entry.getAsJsonObject();
                if (!methods.contains(observation.get("method").getAsString())) continue;
                assertEquals(observation.get("value").getAsInt(), probe.evaluate(observation), observation.toString());
                checked++;
            }
            assertTrue(checked > 0);
        }
    }

    @Test void channelsAndOpaqueMatchTheActualGame() throws Exception { compare(Set.of("alpha", "red", "green", "blue", "opaque")); }
    @Test void packedIntegersAndAlphaReplacementMatchTheActualGame() throws Exception { compare(Set.of("pack4", "pack3", "withAlpha")); }
    @Test void multiplyAndAverageMatchTheActualGame() throws Exception { compare(Set.of("multiply", "average")); }
    @Test void interpolationIncludingNonFiniteFractionsMatchesTheActualGame() throws Exception { compare(Set.of("lerp")); }
    @Test void normalizedFloatsIncludingOutOfRangeValuesMatchTheActualGame() throws Exception { compare(Set.of("fromFloats")); }
}
