package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteOrder;
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
}
