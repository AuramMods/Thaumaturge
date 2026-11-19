package com.leclowndu93150.thaumaturge.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;

public final class WorldRenderPose {
    private WorldRenderPose() {}

    public static PoseStack forStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            // LevelRenderer has already applied the camera pose to RenderSystem for weather.
            return new PoseStack();
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            // GameRenderer posts this stage with its projection stack after the world model-view has been popped.
            // It saved the inverse of the world's camera rotation, including Forge camera roll.
            var rotation = new Matrix3f(RenderSystem.getInverseViewRotationMatrix()).invert();
            var pose = new PoseStack();
            pose.last().pose().set(rotation);
            pose.last().normal().set(rotation);
            return pose;
        }
        return event.getPoseStack();
    }
}
