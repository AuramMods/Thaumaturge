package com.leclowndu93150.thaumaturge.mixin.client.dh;

import com.leclowndu93150.thaumaturge.client.warp.WarpFogState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.core.render.renderer.shaders.FogShader", remap = false)
abstract class LegacyFogShaderMixin {
    @Unique private static final String SHADER = "Lcom/seibel/distanthorizons/core/render/renderer/shaders/FogShader;";
    @Unique private static final String UNIFORM =
            "Lcom/seibel/distanthorizons/core/render/glObject/shader/ShaderProgram;setUniform(IF)V";

    @ModifyArg(method = "onApplyUniforms", at = @At(value = "INVOKE", target = UNIFORM), index = 1,
            slice = @Slice(from = @At(value = "FIELD", target = SHADER + "uFarFogStart:I"),
                    to = @At(value = "FIELD", target = SHADER + "uFarFogMin:I")), require = 2)
    private float thaumaturge$mistDistance(float distance) {
        return Mth.lerp(WarpFogState.visibleIntensity(), distance, 0.0F);
    }

    @ModifyArg(method = "onApplyUniforms", at = @At(value = "INVOKE", target = UNIFORM), index = 1,
            slice = @Slice(from = @At(value = "FIELD", target = SHADER + "uFarFogMin:I"),
                    to = @At(value = "FIELD", target = SHADER + "uFarFogRange:I")))
    private float thaumaturge$mistMinimum(float minimum) {
        return Mth.lerp(WarpFogState.visibleIntensity(), minimum, 1.0F);
    }

    @ModifyArg(method = "onApplyUniforms", at = @At(value = "INVOKE", target = UNIFORM), index = 1,
            slice = @Slice(from = @At(value = "FIELD", target = SHADER + "uFarFogRange:I"),
                    to = @At(value = "FIELD", target = SHADER + "uFarFogDensity:I")))
    private float thaumaturge$mistRange(float range) {
        return Mth.lerp(WarpFogState.visibleIntensity(), range, 0.0F);
    }
}
