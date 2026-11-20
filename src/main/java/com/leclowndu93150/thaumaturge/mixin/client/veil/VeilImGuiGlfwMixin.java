package com.leclowndu93150.thaumaturge.mixin.client.veil;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.PointerBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "imgui.glfw.ImGuiImplGlfw", remap = false)
abstract class VeilImGuiGlfwMixin {
    @WrapOperation(method = "updateMonitors", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/PointerBuffer;limit()I"))
    private int thaumaturge$countAvailableMonitors(PointerBuffer monitors, Operation<Integer> original) {
        // GLFW returns null when no monitors are available. ImGui can still use its main viewport.
        return monitors == null ? 0 : original.call(monitors);
    }
}
