package com.leclowndu93150.thaumaturge.mixin.client.iris;

import com.leclowndu93150.thaumaturge.client.render.TTShaders;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.gl.blending.DepthColorStorage;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Oculus locks color and depth writes at the end of core shader application when a shader pack overrides shaders.
 * Restore the render type's write masks after that hook for Thaumaturge's registered effect shaders.
 */
@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {
    @WrapMethod(method = "apply")
    private void thaumaturge$allowCustomEffectShaders(Operation<Void> original) {
        original.call();
        ShaderInstance shader = (ShaderInstance) (Object) this;
        if (TTShaders.isIrisAllowedCustomEffectShader(shader)
                && !IrisCompat.isShadowPass() && IrisCompat.overridesShaders()) {
            DepthColorStorage.unlockDepthColor();
        }
    }
}
