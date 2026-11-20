package com.leclowndu93150.thaumaturge.mixin.client.veil;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.VeilShaders;
import foundry.veil.api.client.render.shader.program.ProgramDefinition;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShaderManager.class, remap = false)
abstract class VeilShaderManagerMixin {
    @Shadow @Final
    private Map<ResourceLocation, ShaderProgram> shaders;

    @ModifyExpressionValue(method = {"apply", "applyRecompile"}, at = @At(value = "INVOKE",
            target = "Lfoundry/veil/api/client/render/shader/ShaderManager$ReloadState;definitions()Ljava/util/Map;"),
            require = 2)
    private Map<ResourceLocation, ProgramDefinition> thaumaturge$skipUnsupportedCullingProgram(
            Map<ResourceLocation, ProgramDefinition> definitions) {
        ResourceLocation id = VeilShaders.LIGHT_INDIRECT_SPHERE;
        ProgramDefinition definition = definitions.get(id);
        if (definition == null || definition.compute() == null || VeilRenderSystem.computeSupported()) {
            return definitions;
        }

        // The render thread has the GL context. Veil selects its own fallback for culling.
        ShaderProgram previous = this.shaders.remove(id);
        if (previous != null) {
            previous.free();
        }
        Map<ResourceLocation, ProgramDefinition> supported = new LinkedHashMap<>(definitions);
        supported.remove(id);
        return supported;
    }
}
