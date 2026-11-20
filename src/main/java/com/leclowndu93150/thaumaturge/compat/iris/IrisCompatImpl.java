package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.client.render.NodeTexture;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.EntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

/** Oculus render hooks, reached only after Forge confirms Oculus is loaded. */
final class IrisCompatImpl {
    private static final String ALPHA_TEST_UNIFORM = "AlphaTestValue";
    private static final ResourceLocation NODE_TEXTURE = NodeTexture.TEXTURE;

    private IrisCompatImpl() {}

    public static boolean isBatchingEntities(MultiBufferSource.BufferSource buffers) {
        return buffers instanceof FullyBufferedMultiBufferSource;
    }

    public static boolean shadersActive() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    public static boolean overridesShaders() {
        return isShadowPass()
                || Iris.getPipelineManager().getPipelineNullable() instanceof ShaderRenderingPipeline pipeline
                        && pipeline.shouldOverrideShaders();
    }

    public static boolean isShadowPass() {
        return IrisApi.getInstance().isRenderingShadowPass();
    }

    public static boolean isSolidHandPass() {
        return shadersActive() && HandRenderer.INSTANCE.isActive() && HandRenderer.INSTANCE.isRenderingSolid();
    }

    public static ShaderInstance particleTranslucentShader() {
        return ShaderAccess.getParticleTranslucentShader();
    }

    public static boolean isNodeParticleShaderPass(Object shader) {
        if (!shadersActive() || shader != particleTranslucentShader()) {
            return false;
        }
        return RenderSystem.getShaderTexture(0) == nodeTextureId();
    }

    public static boolean isNodeTextureBound() {
        return shadersActive() && RenderSystem.getShaderTexture(0) == nodeTextureId();
    }

    public static void setParticleAlphaTest(float threshold) {
        if (!shadersActive()) {
            return;
        }
        ShaderInstance shader = particleTranslucentShader();
        Uniform alphaTest = shader == null ? null : shader.getUniform(ALPHA_TEST_UNIFORM);
        if (alphaTest != null) {
            alphaTest.set(threshold);
        }
    }

    public static MultiBufferSource entityEffectBuffers(MultiBufferSource buffers) {
        return renderType -> buffers.getBuffer(OuterWrappedRenderType.wrapExactlyOnce(
                "iris:thaumaturge_entity_effect", renderType, EntityRenderStateShard.INSTANCE));
    }

    public static MultiBufferSource blockEntityEffectBuffers(MultiBufferSource buffers) {
        return renderType -> buffers.getBuffer(OuterWrappedRenderType.wrapExactlyOnce(
                "iris:thaumaturge_block_entity_effect", renderType, BlockEntityRenderStateShard.INSTANCE));
    }

    private static int nodeTextureId() {
        return Minecraft.getInstance()
                .getTextureManager()
                .getTexture(NODE_TEXTURE)
                .getId();
    }
}
