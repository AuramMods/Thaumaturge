package com.leclowndu93150.thaumaturge.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor.ARGB32;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class ParticleQuads {
    static final float SPRITE_UV_RANGE = 16.0F;

    private ParticleQuads() {}

    static void draw(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float size,
            float u0, float u1, float v0, float v1, int argb, int light) {
        draw(buffer, rotation, x, y, z, size, u0, u1, v0, v1,
                ARGB32.red(argb), ARGB32.green(argb), ARGB32.blue(argb), ARGB32.alpha(argb), light, false);
    }

    static void draw(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float size,
            float u0, float u1, float v0, float v1, float red, float green, float blue, float alpha, int light,
            boolean reverseWinding) {
        draw(buffer, rotation, x, y, z, size, u0, u1, v0, v1,
                (int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), (int) (alpha * 255.0F), light,
                reverseWinding);
    }

    private static void draw(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float size,
            float u0, float u1, float v0, float v1, int red, int green, int blue, int alpha, int light,
            boolean reverseWinding) {
        // The native terrain sheet culls faces. Use its winding and keep the source UV at each corner.
        float firstX = reverseWinding ? -1.0F : 1.0F;
        float firstU = reverseWinding ? u0 : u1;
        float secondU = reverseWinding ? u1 : u0;
        var pos = new Vector3f();
        vertex(buffer, pos.set(firstX, -1.0F, 0.0F).rotate(rotation).mul(size).add(x, y, z), firstU, v1,
                red, green, blue, alpha, light);
        vertex(buffer, pos.set(firstX, 1.0F, 0.0F).rotate(rotation).mul(size).add(x, y, z), firstU, v0,
                red, green, blue, alpha, light);
        vertex(buffer, pos.set(-firstX, 1.0F, 0.0F).rotate(rotation).mul(size).add(x, y, z), secondU, v0,
                red, green, blue, alpha, light);
        vertex(buffer, pos.set(-firstX, -1.0F, 0.0F).rotate(rotation).mul(size).add(x, y, z), secondU, v1,
                red, green, blue, alpha, light);
    }

    private static void vertex(VertexConsumer buffer, Vector3f pos, float u, float v,
            int red, int green, int blue, int alpha, int light) {
        buffer.vertex(pos.x(), pos.y(), pos.z()).uv(u, v).color(red, green, blue, alpha).uv2(light).endVertex();
    }
}
