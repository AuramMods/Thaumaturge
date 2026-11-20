package com.leclowndu93150.thaumaturge.mixin.client.veil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "foundry.veil.api.client.render.shader.program.ProgramDefinition$Deserializer", remap = false)
abstract class VeilScreenShaderMixin {
    @Redirect(method = "deserializeSource", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lcom/google/gson/JsonObject;get(Ljava/lang/String;)Lcom/google/gson/JsonElement;"))
    private static JsonElement thaumaturge$readScreenVertexPath(JsonObject source, String key) {
        JsonElement path = source.get(key);
        if (path != null) {
            return path;
        }
        // Veil 1.0.0.296 ships this vertex using "location" while its parser reads "path".
        JsonElement location = source.get("location");
        if (location instanceof JsonPrimitive name && name.isString() && "veil:blit_screen".equals(name.getAsString())) {
            return location;
        }
        return null;
    }
}
