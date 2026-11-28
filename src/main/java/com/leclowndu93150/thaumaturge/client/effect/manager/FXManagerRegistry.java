package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.particle.EssentiaParticles;
import com.leclowndu93150.thaumaturge.client.particle.SparkleParticle;
import com.leclowndu93150.thaumaturge.client.render.WorldRenderPose;
import com.leclowndu93150.thaumaturge.compat.auram.AuramVisualSources;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class FXManagerRegistry {
    private static final MultiBufferSource.BufferSource AFTER_LEVEL_BUFFERS =
            MultiBufferSource.immediate(new BufferBuilder(8192));
    private static final List<AbstractFXManager<?>> MANAGERS = List.of(
            BeamManager.INSTANCE,
            EssentiaStreamManager.INSTANCE,
            BoreVoidStreamManager.INSTANCE,
            BoreDigEffectManager.INSTANCE);

    private FXManagerRegistry() {}

    @SubscribeEvent
    public static void onTick(LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ClientLevel cl)) return;
        for (AbstractFXManager<?> m : MANAGERS) m.tickAll(cl);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !AuramVisualSources.available()) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.isPaused()) return;
        AuramVisualSources.beginEssentiaTick(level);
        EssentiaStreamManager.INSTANCE.collectVisualSources(level);
        EssentiaParticles.collectVisualSources(level, mc.particleEngine);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();
        boolean shaders = IrisCompat.shadersActive();
        for (AbstractFXManager<?> m : MANAGERS) {
            if (!shaders || m != EssentiaStreamManager.INSTANCE) m.renderAll(poseStack, camera, partialTick);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void renderAfterLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL
                || !IrisCompat.shadersActive() || IrisCompat.isShadowPass()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        // Draw after shader composition, using the local world depth on the main target.
        mc.getMainRenderTarget().bindWrite(false);
        PoseStack pose = WorldRenderPose.forStage(event);
        Camera camera = event.getCamera();
        float partialTick = event.getPartialTick();
        EssentiaStreamManager.INSTANCE.render(pose, camera, partialTick, AFTER_LEVEL_BUFFERS);
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(pose.last().pose());
        RenderSystem.applyModelViewMatrix();
        try {
            EssentiaParticles.renderAfterLevel(mc.particleEngine, AFTER_LEVEL_BUFFERS, camera, partialTick);
            SparkleParticle.renderAfterLevel(mc.particleEngine, AFTER_LEVEL_BUFFERS, camera, partialTick);
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ClientLevel level) AuramVisualSources.clear(level);
    }

    public static void clearAll() {
        for (AbstractFXManager<?> manager : MANAGERS) {
            manager.clear();
        }
    }
}
