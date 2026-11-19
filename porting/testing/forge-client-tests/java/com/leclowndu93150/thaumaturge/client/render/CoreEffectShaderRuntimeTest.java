package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.util.GsonHelper;
import org.junit.jupiter.api.Test;

class CoreEffectShaderRuntimeTest {
    @Test
    void coreEffectsDeclareTheirNativeVertexAttributes() throws Exception {
        Path directory = Path.of(System.getProperty("thaumaturge.testResourceRoot"),
                "assets/thaumaturge/shaders/core");
        Map<String, VertexFormat> formats = Map.of(
                "tt_ender", DefaultVertexFormat.POSITION,
                "tt_portal", DefaultVertexFormat.POSITION,
                "tt_entity_flat", DefaultVertexFormat.NEW_ENTITY,
                "tt_occluding_effect", DefaultVertexFormat.NEW_ENTITY,
                "tt_fx", DefaultVertexFormat.PARTICLE,
                "tt_fx_alpha_test", DefaultVertexFormat.PARTICLE,
                "void_stream", DefaultVertexFormat.POSITION_TEX_COLOR,
                "ward_add", DefaultVertexFormat.POSITION_TEX_COLOR);
        for (var shader : formats.entrySet()) {
            Path file = directory.resolve(shader.getKey() + ".json");
            var definition = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            var attributes = GsonHelper.getAsJsonArray(definition, "attributes").asList().stream()
                    .map(JsonElement::getAsString).toList();
            var formatAttributes = shader.getValue().getElementAttributeNames().stream()
                    .filter(attribute -> !attribute.equals("Padding")).toList();
            assertEquals(formatAttributes, attributes, shader.getKey());
        }
    }
}
