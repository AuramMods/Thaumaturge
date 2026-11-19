package com.leclowndu93150.thaumaturge.mixin.client.iris;

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
 * Iris's translucent-particle program uses a 0.1 alpha test. The node atlas deliberately uses alpha 0.1 for its
 * unrevealed aura and contains still softer texels in the revealed aspect layer, so the stock Iris cutoff erases those
 * parts. Shader packs may also replace the RenderType's blend mode after it has been configured.
 *
 * <p>This compatibility hook is texture-scoped to Thaumaturge's node atlas. It therefore leaves ordinary particles
 * and other shader-pack effects untouched.</p>
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.programs.ExtendedShader", remap = false)
public abstract class ExtendedShaderMixin {
    private static final float NODE_ALPHA_TEST = 0.0001F;

    @ModifyArg(
            method = {"apply()V", "m_173363_()V"},
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/irisshaders/iris/uniforms/CapturedRenderingState;setCurrentAlphaTest(F)V"),
            index = 0)
    private float thaumaturge$preserveNodeAlpha(float irisAlphaTest) {
        return IrisCompat.isNodeParticleShaderPass(this) ? Math.min(irisAlphaTest, NODE_ALPHA_TEST) : irisAlphaTest;
    }

    @WrapWithCondition(
            method = {"apply()V", "m_173363_()V"},
            at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/blending/BlendModeOverride;apply()V"),
            remap = false)
    private boolean thaumaturge$allowBlendOverride(BlendModeOverride override) {
        return !IrisCompat.isNodeParticleShaderPass(this);
    }

    @WrapWithCondition(
            method = {"apply()V", "m_173363_()V"},
            at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"),
            remap = false)
    private boolean thaumaturge$allowBufferBlendOverrides(
            List<BufferBlendOverride> overrides, Consumer<BufferBlendOverride> apply) {
        return !IrisCompat.isNodeParticleShaderPass(this);
    }
}
