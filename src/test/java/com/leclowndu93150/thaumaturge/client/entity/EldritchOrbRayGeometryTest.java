package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteOrder;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

class EldritchOrbRayGeometryTest {
    @Test
    void rayTrianglesCompleteTheirPositionsAndPackedColors() {
        var format = DefaultVertexFormat.POSITION_COLOR;
        var buffer = new BufferBuilder(64);
        buffer.begin(VertexFormat.Mode.TRIANGLES, format);
        EldritchOrbRayGeometry.render(buffer, new Matrix4f().translation(3, 5, 7), 1, 1.0F, 0x80402010, 0x40010203);
        var rendered = buffer.end();
        try {
            assertEquals(9, rendered.drawState().vertexCount());
            var bytes = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
            assertEquals(3.0F, bytes.getFloat(0));
            assertEquals(5.0F, bytes.getFloat(Float.BYTES));
            assertEquals(7.0F, bytes.getFloat(2 * Float.BYTES));
            int color = format.getOffset(1);
            assertEquals(0x40, Byte.toUnsignedInt(bytes.get(color)));
            assertEquals(0x20, Byte.toUnsignedInt(bytes.get(color + 1)));
            assertEquals(0x10, Byte.toUnsignedInt(bytes.get(color + 2)));
            assertEquals(0x80, Byte.toUnsignedInt(bytes.get(color + 3)));
            int tip = format.getVertexSize() + color;
            assertEquals(0x01, Byte.toUnsignedInt(bytes.get(tip)));
            assertEquals(0x40, Byte.toUnsignedInt(bytes.get(tip + 3)));
        } finally {
            rendered.release();
        }
    }

    @Test
    void precomputedTipsAreFiniteAndStayWithinTheRayEnvelope() {
        float[] tips = EldritchOrbRayGeometry.tipsForTest();

        assertEquals(16 * 3 * 3, tips.length);
        for (int offset = 0; offset < tips.length; offset += 3) {
            float x = tips[offset];
            float y = tips[offset + 1];
            float z = tips[offset + 2];
            float radius = (float) Math.sqrt(x * x + y * y + z * z);

            assertTrue(Float.isFinite(radius));
            assertTrue(radius >= 0.15F);
            assertTrue(radius <= 1.1F);
        }
    }

    @Test
    void testRayTipsAreDefensiveCopies() {
        float[] first = EldritchOrbRayGeometry.tipsForTest();
        float expected = first[0];
        first[0] = Float.NaN;

        assertEquals(expected, EldritchOrbRayGeometry.tipsForTest()[0]);
    }
}
