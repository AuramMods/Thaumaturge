package com.leclowndu93150.thaumaturge.mixin.client.iris;

import com.leclowndu93150.thaumaturge.client.particle.ParticleSheet;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import java.util.List;
import java.util.function.Consumer;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Preserve the node atlas and custom sheets' soft alpha, and keep the blend mode for additive effects.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.programs.ExtendedShader", remap = false)
public abstract class ExtendedShaderMixin {
    private static final float SOFT_ALPHA_TEST = 0.0001F;

    @ModifyArg(
            method = {"apply()V", "m_173363_()V"},
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/irisshaders/iris/uniforms/CapturedRenderingState;setCurrentAlphaTest(F)V"),
            index = 0)
    private float thaumaturge$preserveParticleAlpha(float irisAlphaTest) {
        return ParticleSheet.isShaderPackDraw() || IrisCompat.isNodeParticleShaderPass(this)
                ? Math.min(irisAlphaTest, SOFT_ALPHA_TEST) : irisAlphaTest;
    }

    @WrapWithCondition(
            method = {"apply()V", "m_173363_()V"},
            at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/blending/BlendModeOverride;apply()V"),
            remap = false)
    private boolean thaumaturge$allowBlendOverride(BlendModeOverride override) {
        return !ParticleSheet.isAdditiveShaderPackDraw() && !IrisCompat.isNodeParticleShaderPass(this);
    }

    @WrapWithCondition(
            method = {"apply()V", "m_173363_()V"},
            at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"),
            remap = false)
    private boolean thaumaturge$allowBufferBlendOverrides(
            List<BufferBlendOverride> overrides, Consumer<BufferBlendOverride> apply) {
        return !ParticleSheet.isAdditiveShaderPackDraw() && !IrisCompat.isNodeParticleShaderPass(this);
    }
}
