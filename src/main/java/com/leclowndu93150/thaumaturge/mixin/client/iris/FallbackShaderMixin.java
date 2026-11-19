package com.leclowndu93150.thaumaturge.mixin.client.iris;

import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Preserves Thaumaturge node blending when Iris has to use its fallback particle shader. */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.programs.FallbackShader", remap = false)
public abstract class FallbackShaderMixin {
    @WrapWithCondition(
            method = {"apply()V", "m_173363_()V"},
            at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/blending/BlendModeOverride;apply()V"),
            remap = false)
    private boolean thaumaturge$allowBlendOverride(BlendModeOverride override) {
        return !IrisCompat.isNodeTextureBound();
    }
}
