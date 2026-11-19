package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.compat.Colors.ARGB32;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

public final class ModelRender {
    private ModelRender() {}

    public static void render(Model model, PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        model.renderToBuffer(pose, buffer, light, overlay,
                ARGB32.red(color) / 255.0F, ARGB32.green(color) / 255.0F,
                ARGB32.blue(color) / 255.0F, ARGB32.alpha(color) / 255.0F);
    }

    public static void render(ModelPart part, PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        part.render(pose, buffer, light, overlay,
                ARGB32.red(color) / 255.0F, ARGB32.green(color) / 255.0F,
                ARGB32.blue(color) / 255.0F, ARGB32.alpha(color) / 255.0F);
    }
}
