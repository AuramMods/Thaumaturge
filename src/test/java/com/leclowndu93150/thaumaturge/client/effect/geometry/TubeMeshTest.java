package com.leclowndu93150.thaumaturge.client.effect.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteOrder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.invocation.Invocation;

class TubeMeshTest {
    private static final float[][] COLOURS = {
        {1.0F, 0.5F, 0.25F, 1.0F},
        {1.0F, 0.5F, 0.25F, 1.0F},
        {1.0F, 0.5F, 0.25F, 1.0F},
        {1.0F, 0.5F, 0.25F, 1.0F}
    };
    private static final double[] RADII = {0.1, 0.1, 0.1, 0.1};

    @Test
    void emitsFiniteTubeVerticesForCurvedPath() {
        VertexConsumer consumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
        double[][] points = {{0.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {1.0, 2.0, 0.0}, {1.0, 3.0, 1.0}};

        TubeMesh.draw(new PoseStack(), consumer, points, COLOURS, RADII, 0x00F000F0, 1.0F, 0.0F);

        List<Invocation> vertices = mockingDetails(consumer).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("vertex"))
                .toList();
        assertEquals((points.length - 3) * TubeMesh.ROUND_SIDES * 4, vertices.size());
        assertTrue(vertices.stream().allMatch(TubeMeshTest::hasFinitePosition));
    }

    @Test
    void skipsRepeatedPathPointsAndRejectsInvalidCoordinates() {
        VertexConsumer consumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
        double[][] repeated = {{0.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 1.0, 0.0}, {1.0, 2.0, 0.0}, {1.0, 3.0, 0.0}};
        float[][] colours = {COLOURS[0], COLOURS[1], COLOURS[1], COLOURS[2], COLOURS[3]};
        double[] radii = {0.1, 0.1, 0.1, 0.1, 0.1};

        TubeMesh.draw(new PoseStack(), consumer, repeated, colours, radii, 0, 1.0F, 0.0F);

        List<Invocation> vertices = mockingDetails(consumer).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("vertex"))
                .toList();
        assertEquals((4 - 3) * TubeMesh.ROUND_SIDES * 4, vertices.size());
        assertTrue(vertices.stream().allMatch(TubeMeshTest::hasFinitePosition));

        VertexConsumer invalidConsumer = mock(VertexConsumer.class, Answers.RETURNS_SELF);
        double[][] invalid = {{0.0, 0.0, 0.0}, {Double.NaN, 1.0, 0.0}, {1.0, 2.0, 0.0}};
        TubeMesh.draw(new PoseStack(), invalidConsumer, invalid, COLOURS, RADII, 0, 1.0F, 0.0F);
        assertFalse(mockingDetails(invalidConsumer).getInvocations().stream()
                .anyMatch(invocation -> invocation.getMethod().getName().equals("vertex")));
    }

    @Test
    void writesCompleteVerticesForLitAndUnlitTubeRenderTypes() {
        double[][] points = {{0.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {1.0, 2.0, 0.0}, {1.0, 3.0, 1.0}};
        var buffer = new BufferBuilder(128);
        for (var format : List.of(DefaultVertexFormat.PARTICLE, DefaultVertexFormat.POSITION_TEX_COLOR)) {
            buffer.begin(VertexFormat.Mode.QUADS, format);
            TubeMesh.draw(new PoseStack(), buffer, points, COLOURS, RADII, 0x00F00070, 1.0F, 0.0F);
            var rendered = buffer.end();
            try {
                assertEquals(TubeMesh.ROUND_SIDES * 4, rendered.drawState().vertexCount());
                var bytes = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
                int color = format.getOffset(2);
                assertEquals(255, Byte.toUnsignedInt(bytes.get(color)));
                assertEquals(127, Byte.toUnsignedInt(bytes.get(color + 1)));
                assertEquals(63, Byte.toUnsignedInt(bytes.get(color + 2)));
                assertEquals(255, Byte.toUnsignedInt(bytes.get(color + 3)));
                if (format == DefaultVertexFormat.PARTICLE) {
                    int light = format.getOffset(3);
                    assertEquals(0x70, bytes.getShort(light));
                    assertEquals(0xF0, bytes.getShort(light + Short.BYTES));
                }
            } finally {
                rendered.release();
            }
        }
    }

    private static boolean hasFinitePosition(Invocation invocation) {
        Object[] arguments = invocation.getArguments();
        return arguments.length == 4
                && Float.isFinite((float) arguments[1])
                && Float.isFinite((float) arguments[2])
                && Float.isFinite((float) arguments[3]);
    }
}
