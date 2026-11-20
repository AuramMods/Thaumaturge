package com.leclowndu93150.thaumaturge.mixin.client.veil;

import foundry.veil.api.client.render.shader.ShaderCompiler;
import foundry.veil.api.client.render.shader.program.ProgramDefinition;
import foundry.veil.impl.client.render.shader.DirectShaderCompiler;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;

@Mixin(value = DirectShaderCompiler.class, remap = false)
abstract class VeilBlitVaryingMixin {
    @Unique
    private static final ResourceLocation thaumaturge$BLIT_SCREEN = ResourceLocation.fromNamespaceAndPath("veil", "blit_screen");

    @Shadow
    private ResourceLocation compilingName;

    @ModifyArg(
            method = "compile(Lfoundry/veil/api/client/render/shader/ShaderCompiler$Context;ILfoundry/veil/api/client/render/shader/program/ProgramDefinition$SourceType;Lnet/minecraft/resources/ResourceLocation;)Lfoundry/veil/api/client/render/shader/CompiledShader;",
            at = @At(value = "INVOKE", target = "Lfoundry/veil/impl/client/render/shader/DirectShaderCompiler;compile(Lfoundry/veil/api/client/render/shader/ShaderCompiler$Context;ILfoundry/veil/api/client/render/shader/program/ProgramDefinition$SourceType;Ljava/lang/String;)Lfoundry/veil/api/client/render/shader/CompiledShader;"),
            index = 3)
    private String thaumaturge$matchBlitVaryingByName(
            ShaderCompiler.Context context, int shaderType, ProgramDefinition.SourceType sourceType, String source) {
        if (shaderType == GL_VERTEX_SHADER
                && sourceType == ProgramDefinition.SourceType.GLSL
                && thaumaturge$BLIT_SCREEN.equals(this.compilingName)) {
            // Veil's GLSL fragment stages declare texCoord by name with no location qualifier.
            return source.replace("layout(location = 0) out vec2 texCoord;", "out vec2 texCoord;");
        }
        return source;
    }
}
