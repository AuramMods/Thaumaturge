package com.leclowndu93150.thaumaturge.mixin.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.TTParticleLayers;
import com.leclowndu93150.thaumaturge.client.warding.WardClientHandler;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    // Forge adds this overload with the same name in both namespaces.
    @Unique
    private static final String RENDER_WITH_FRUSTUM = "render(Lcom/mojang/blaze3d/vertex/PoseStack;"
            + "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"
            + "Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F"
            + "Lnet/minecraft/client/renderer/culling/Frustum;)V";

    @ModifyExpressionValue(
            method = RENDER_WITH_FRUSTUM,
            at = @At(value = "INVOKE", target = "Ljava/util/Map;keySet()Ljava/util/Set;", remap = false),
            remap = false)
    private Set<ParticleRenderType> thaumaturge$orderNitorLayers(Set<ParticleRenderType> layers) {
        return TTParticleLayers.orderNitorLayers(layers);
    }

    @Inject(method = RENDER_WITH_FRUSTUM, at = @At("RETURN"), remap = false)
    private void thaumaturge$restoreDepthTest(CallbackInfo ci) {
        RenderSystem.enableDepthTest();
    }

    @Inject(method = "crack", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$wardHitEffect(BlockPos pos, Direction direction, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && ClientWardHolder.isWarded(pos)) {
            WardClientHandler.spawnHitEffect(level, pos, direction, null);
            ci.cancel();
        }
    }
}
