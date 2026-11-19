package com.leclowndu93150.thaumaturge.client.model.mesh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.IQuadTransformer;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

class TTMeshQuadBakerRuntimeTest {
    @Test
    void nativeBakerEmitsIndependentQuadsWithAtlasUvAndMetadata() {
        var sprite = mock(TextureAtlasSprite.class);
        var contents = mock(SpriteContents.class);
        when(sprite.contents()).thenReturn(contents);
        when(contents.width()).thenReturn(16);
        when(contents.height()).thenReturn(16);
        when(sprite.getU0()).thenReturn(0.25F);
        when(sprite.getU1()).thenReturn(0.5F);
        when(sprite.getV0()).thenReturn(0.5F);
        when(sprite.getV1()).thenReturn(0.75F);
        when(sprite.getU(anyDouble()))
                .thenAnswer(call -> 0.25F + (float) (call.getArgument(0, Double.class) / 64.0));
        when(sprite.getV(anyDouble()))
                .thenAnswer(call -> 0.5F + (float) (call.getArgument(0, Double.class) / 64.0));

        var part = new TTMeshPart("face", "main", 2,
                new float[] {
                    0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0,
                    2, 0, 0, 3, 0, 0, 3, 1, 0, 2, 1, 0
                },
                new float[] {0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1},
                new float[] {
                    0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1,
                    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1
                });
        var quads = new ArrayList<BakedQuad>();
        TTMeshQuadBaker.bakePart(part, sprite, 2, new Matrix4f().translation(10, 0, 0),
                true, true, 0x70, 0xE0, quads);

        assertEquals(2, quads.size());
        assertNotSame(quads.get(0).getVertices(), quads.get(1).getVertices());
        float inset = 0.125F / 256.0F;
        float[] horizontal = {0.25F + inset, 0.5F - inset, 0.5F - inset, 0.25F + inset};
        float[] vertical = {0.75F - inset, 0.75F - inset, 0.5F + inset, 0.5F + inset};
        float[] x = {0, 1, 1, 0};
        float[] y = {0, 0, 1, 1};
        for (int face = 0; face < quads.size(); face++) {
            BakedQuad quad = quads.get(face);
            assertEquals(Direction.SOUTH, quad.getDirection());
            assertEquals(2, quad.getTintIndex());
            assertSame(sprite, quad.getSprite());
            assertTrue(quad.isShade());
            assertTrue(quad.hasAmbientOcclusion());
            int[] data = quad.getVertices();
            for (int corner = 0; corner < 4; corner++) {
                int offset = corner * IQuadTransformer.STRIDE;
                int position = offset + IQuadTransformer.POSITION;
                int uv = offset + IQuadTransformer.UV0;
                assertEquals(10 + face * 2 + x[corner], Float.intBitsToFloat(data[position]));
                assertEquals(y[corner], Float.intBitsToFloat(data[position + 1]));
                assertEquals(horizontal[corner], Float.intBitsToFloat(data[uv]));
                assertEquals(vertical[corner], Float.intBitsToFloat(data[uv + 1]));
                assertEquals(-1, data[offset + IQuadTransformer.COLOR]);
                assertEquals(0x00E00070, data[offset + IQuadTransformer.UV2]);
                assertEquals(0x007F0000, data[offset + IQuadTransformer.NORMAL]);
            }
        }
    }
}
