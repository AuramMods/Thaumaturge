package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.leclowndu93150.thaumaturge.compat.Colors;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;

class ModelRenderRuntimeTest {
    @Test
    void packedColorReachesEveryNativeModelVertexWithItsOriginalLightAndOverlay() {
        var mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 2, 2, 2), PartPose.ZERO);
        var part = LayerDefinition.create(mesh, 16, 16).bakeRoot();
        var model = new Model(RenderType::entityCutout) {
            @Override
            public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,
                    float red, float green, float blue, float alpha) {
                part.render(pose, buffer, light, overlay, red, green, blue, alpha);
            }
        };
        var consumer = mock(VertexConsumer.class);
        var pose = new PoseStack();
        int color = 0x802349B1;
        int light = 0x005000A0;
        int overlay = 0x00030004;

        ModelRender.render(part, pose, consumer, light, overlay, color);
        ModelRender.render(model, pose, consumer, light, overlay, color);

        verify(consumer, times(48)).vertex(anyFloat(), anyFloat(), anyFloat(),
                eq(0x23 / 255.0F), eq(0x49 / 255.0F), eq(0xB1 / 255.0F), eq(0x80 / 255.0F),
                anyFloat(), anyFloat(), eq(overlay), eq(light), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    void nativeDiffuseDyesAndImageChannelOrderMatchThePinnedColors() {
        int[] pinnedById = {
                0xfff9fffe, 0xfff9801d, 0xffc74ebd, 0xff3ab3da, 0xfffed83d, 0xff80c71f, 0xfff38baa, 0xff474f52,
                0xff9d9d97, 0xff169c9c, 0xff8932b8, 0xff3c44aa, 0xff835432, 0xff5e7c16, 0xffb02e26, 0xff1d1d21
        };
        for (DyeColor dye : DyeColor.values()) {
            assertEquals(pinnedById[dye.getId()], Colors.ARGB32.dyeColor(dye), dye.getName());
        }
        assertEquals(0x80B14923, Colors.swapRedBlue(0x802349B1));
        assertEquals(0x802349B1, Colors.swapRedBlue(0x80B14923));
    }
}
