package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.fml.ModList;

public final class IrisCompat {
    private IrisCompat() {}

    public static boolean shadersActive() {
        return isLoaded() && IrisCompatImpl.shadersActive();
    }

    public static boolean overridesShaders() {
        return isLoaded() && IrisCompatImpl.overridesShaders();
    }

    public static boolean isShadowPass() {
        return isLoaded() && IrisCompatImpl.isShadowPass();
    }

    public static boolean isSolidHandPass() {
        return isLoaded() && IrisCompatImpl.isSolidHandPass();
    }

    /** Checks the current frame's level buffer even when no shader pack is active. */
    public static boolean isBatchingEntities(MultiBufferSource.BufferSource buffers) {
        return isLoaded() && IrisCompatImpl.isBatchingEntities(buffers);
    }

    /**
     * Gets Oculus's translucent particle program for Thaumaturge's particle-format effects.
     */
    public static ShaderInstance particleTranslucentShader() {
        return isLoaded() ? IrisCompatImpl.particleTranslucentShader() : null;
    }

    /**
     * Returns true only while Iris is applying its translucent-particle shader to Thaumaturge's node atlas.
     *
     * <p>Using the currently bound texture is intentional. Iris applies the shader after the RenderType has already
     * bound its texture, so this remains correct even when Iris buffers and reorders block-entity draws. It also avoids
     * changing the shader-pack particle rules for vanilla particles or unrelated Thaumaturge effects.</p>
     */
    public static boolean isNodeParticleShaderPass(Object shader) {
        return isLoaded() && IrisCompatImpl.isNodeParticleShaderPass(shader);
    }

    /** True while the node atlas is bound, used by Iris fallback-shader compatibility. */
    public static boolean isNodeTextureBound() {
        return isLoaded() && IrisCompatImpl.isNodeTextureBound();
    }

    /** Sets the Iris particle alpha cutoff for the duration of a Thaumaturge effect render type. */
    public static void setParticleAlphaTest(float threshold) {
        if (isLoaded()) {
            IrisCompatImpl.setParticleAlphaTest(threshold);
        }
    }

    /**
     * Adds Iris's entity render context to deferred visual effects. Effects submitted after the normal entity pass do
     * not pass through Iris's {@code EntityRenderDispatcher} hook, so they need the same wrapper explicitly.
     */
    public static MultiBufferSource entityEffectBuffers(MultiBufferSource buffers) {
        return shadersActive() ? IrisCompatImpl.entityEffectBuffers(buffers) : buffers;
    }

    /**
     * Adds Iris's block-entity render context to deferred visual effects. Effects submitted after the normal block
     * entity pass do not pass through Iris's {@code BlockEntityRenderDispatcher} hook.
     */
    public static MultiBufferSource blockEntityEffectBuffers(MultiBufferSource buffers) {
        return shadersActive() ? IrisCompatImpl.blockEntityEffectBuffers(buffers) : buffers;
    }

    private static boolean isLoaded() {
        return ModList.get().isLoaded(TTIds.OCULUS);
    }
}
