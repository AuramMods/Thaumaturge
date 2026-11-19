package com.leclowndu93150.thaumaturge.client.effect.manager;

import net.minecraftforge.event.TickEvent;
import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class FXManagerRegistry {
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

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();
        for (AbstractFXManager<?> m : MANAGERS) m.renderAll(poseStack, camera, partialTick);
    }

    public static void clearAll() {
        for (AbstractFXManager<?> manager : MANAGERS) {
            manager.clear();
        }
    }
}
