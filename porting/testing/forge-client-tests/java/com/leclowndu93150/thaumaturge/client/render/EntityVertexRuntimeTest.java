package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.leclowndu93150.thaumaturge.client.render.entity.TintBufferSource;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class EntityVertexRuntimeTest {
    @Test
    void entityFormatPreservesTintUvOverlayLightAndReflectedNormal() {
        var stack = new PoseStack();
        stack.translate(5, 6, 7);
        stack.scale(-2, 3, 4);
        var format = DefaultVertexFormat.NEW_ENTITY;
        var pose = stack.last();
        var buffer = new BufferBuilder(256);
        buffer.begin(VertexFormat.Mode.QUADS, format);
        for (int i = 0; i < 4; i++) {
            EntityVertices.finish(
                    buffer.vertex(pose.pose(), 1 + i, 2, 3)
                            .color(0x40102030)
                            .uv(0.125F, 0.75F)
                            .overlayCoords(0x00040009)
                            .uv2(0x00E00070),
                    pose, 1, 1, 0);
        }

        var rendered = buffer.end();
        try {
            assertEquals(4, rendered.drawState().vertexCount());
            var bytes = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
            assertEquals(3.0F, bytes.getFloat(0));
            assertEquals(12.0F, bytes.getFloat(4));
            assertEquals(19.0F, bytes.getFloat(8));
            int color = format.getOffset(1);
            assertEquals(0x10, bytes.get(color));
            assertEquals(0x20, bytes.get(color + 1));
            assertEquals(0x30, bytes.get(color + 2));
            assertEquals(0x40, bytes.get(color + 3));
            int uv = format.getOffset(2);
            assertEquals(0.125F, bytes.getFloat(uv));
            assertEquals(0.75F, bytes.getFloat(uv + Float.BYTES));
            assertEquals(0x00040009, bytes.getInt(format.getOffset(3)));
            assertEquals(0x00E00070, bytes.getInt(format.getOffset(4)));
            int normal = format.getOffset(5);
            assertEquals(-105, bytes.get(normal));
            assertEquals(70, bytes.get(normal + 1));
            assertEquals(0, bytes.get(normal + 2));
        } finally {
            rendered.release();
        }
    }

    @Test
    void consecutiveAxisReflectionsKeepBothNormalSigns() {
        var stack = new PoseStack();
        stack.scale(-1, 1, 1);
        stack.scale(1, -1, 1);
        var pose = stack.last();
        var format = DefaultVertexFormat.NEW_ENTITY;
        var buffer = new BufferBuilder(256);
        buffer.begin(VertexFormat.Mode.QUADS, format);
        for (int i = 0; i < 4; i++) {
            EntityVertices.finish(
                    buffer.vertex(pose.pose(), i, 0, 0)
                            .color(-1).uv(0, 0).overlayCoords(0).uv2(0),
                    pose, 0.6F, 0.8F, 0);
        }

        var rendered = buffer.end();
        try {
            var bytes = rendered.vertexBuffer();
            int normal = format.getOffset(5);
            assertEquals(-76, bytes.get(normal));
            assertEquals(-101, bytes.get(normal + 1));
            assertEquals(0, bytes.get(normal + 2));
        } finally {
            rendered.release();
        }
    }

    @Test
    void forgeBufferTintsPackedVerticesAndNativeModelPartColors() {
        var buffer = new BufferBuilder(2048);
        var format = DefaultVertexFormat.NEW_ENTITY;
        var layer = RenderType.entityCutoutNoCull(ResourceLocation.withDefaultNamespace("textures/entity/bat.png"));
        var source = new TintBufferSource(requestedLayer -> {
            assertSame(layer, requestedLayer);
            return buffer;
        }, 0x80604020);
        var tinted = source.getBuffer(layer);
        var pose = new PoseStack();
        buffer.begin(VertexFormat.Mode.QUADS, format);
        for (int i = 0; i < 4; i++) {
            EntityVertices.finish(
                    tinted.vertex(pose.last().pose(), i, 0, 0)
                            .color(0x8090C0FF).uv(0, 0).overlayCoords(5).uv2(9),
                    pose.last(), 0, 1, 0);
        }

        var mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0, 0, 0, 2, 2, 2), PartPose.ZERO);
        LayerDefinition.create(mesh, 16, 16).bakeRoot()
                .render(pose, tinted, 9, 5, 1.0F, 0.5F, 0.25F, 0.75F);

        var rendered = buffer.end();
        try {
            assertEquals(28, rendered.drawState().vertexCount());
            var bytes = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
            assertColor(bytes, format.getOffset(1), 54, 48, 32, 64);
            int firstModelVertex = 4 * format.getVertexSize();
            assertColor(bytes, firstModelVertex + format.getOffset(1), 96, 31, 7, 95);
            assertEquals(5, bytes.getInt(firstModelVertex + format.getOffset(3)));
            assertEquals(9, bytes.getInt(firstModelVertex + format.getOffset(4)));
        } finally {
            rendered.release();
        }
    }

    private static void assertColor(ByteBuffer vertices, int offset, int red, int green, int blue, int alpha) {
        assertEquals(red, Byte.toUnsignedInt(vertices.get(offset)));
        assertEquals(green, Byte.toUnsignedInt(vertices.get(offset + 1)));
        assertEquals(blue, Byte.toUnsignedInt(vertices.get(offset + 2)));
        assertEquals(alpha, Byte.toUnsignedInt(vertices.get(offset + 3)));
    }
}
